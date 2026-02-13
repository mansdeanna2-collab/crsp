#!/usr/bin/env python3
"""
Docker自动部署脚本
自动检测项目依赖并打包到Docker环境运行
从sz文件读取域名和SSL证书配置
"""

import os
import re
import subprocess
import sys
import shutil
import stat
import time
from pathlib import Path


class DockerDeployer:
    """Docker自动部署器"""
    
    STANDARD_HTTPS_PORT = 443
    
    def __init__(self, project_dir=None, frontend_port=1000, ssl_port=8443, http_port=80):
        """
        初始化部署器
        
        Args:
            project_dir: 项目目录路径，默认为当前目录
            frontend_port: 前端端口，默认1000
            ssl_port: HTTPS端口(宿主机映射)，默认8443，避免与其他服务的443冲突
            http_port: HTTP端口(宿主机映射)，默认80
        """
        self.project_dir = Path(project_dir) if project_dir else Path.cwd()
        self.frontend_port = frontend_port
        self.ssl_port = ssl_port
        self.http_port = http_port
        self.app_port = 8080  # Spring Boot默认端口
        self.image_name = "crsp-mall"
        self.container_name = "crsp-mall-container"
        
        # 从sz文件读取配置
        self.domain = None
        self.ssl_fullchain = None
        self.ssl_privkey = None
        self._load_sz_config()
        
        # 设置Docker API版本
        os.environ["DOCKER_API_VERSION"] = "1.42"
        
    def _load_sz_config(self):
        """从sz文件读取域名和SSL证书配置"""
        sz_path = self.project_dir / "sz"
        if not sz_path.exists():
            print("⚠️  sz配置文件不存在，跳过域名和SSL配置")
            return
        
        print("📄 从sz文件读取域名和SSL配置...")
        sz_content = sz_path.read_text(encoding="utf-8")
        
        # 提取域名 (DOMAIN="...")
        domain_match = re.search(r'DOMAIN="([^"]+)"', sz_content)
        if domain_match:
            self.domain = domain_match.group(1)
            print(f"✅ 域名: {self.domain}")
        
        # 提取SSL fullchain证书 (在CERT_EOF heredoc中)
        cert_match = re.search(
            r"cat\s*>\s*\S+fullchain\.pem\S*\s*<<\s*'CERT_EOF'\s*\n(.*?)\nCERT_EOF",
            sz_content, re.DOTALL
        )
        if cert_match:
            self.ssl_fullchain = cert_match.group(1).strip()
            print("✅ SSL证书(fullchain)已读取")
        
        # 提取SSL私钥 (在KEY_EOF heredoc中)
        key_match = re.search(
            r"cat\s*>\s*\S+privkey\.pem\S*\s*<<\s*'KEY_EOF'\s*\n(.*?)\nKEY_EOF",
            sz_content, re.DOTALL
        )
        if key_match:
            self.ssl_privkey = key_match.group(1).strip()
            print("✅ SSL私钥已读取")
    
    def setup_ssl(self):
        """将从sz文件读取的SSL证书写入ssl目录"""
        if not self.ssl_fullchain or not self.ssl_privkey:
            print("⚠️  SSL证书或私钥未配置，跳过SSL设置")
            return False
        
        ssl_dir = self.project_dir / "ssl"
        ssl_dir.mkdir(exist_ok=True)
        
        # 写入fullchain证书
        fullchain_path = ssl_dir / "fullchain.pem"
        fullchain_path.write_text(self.ssl_fullchain + "\n", encoding="utf-8")
        fullchain_path.chmod(stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP | stat.S_IROTH)  # chmod 644
        
        # 写入私钥并设置权限
        privkey_path = ssl_dir / "privkey.pem"
        privkey_path.write_text(self.ssl_privkey + "\n", encoding="utf-8")
        privkey_path.chmod(stat.S_IRUSR | stat.S_IWUSR)  # chmod 600
        
        print(f"✅ SSL证书已写入: {ssl_dir}")
        return True
    
    def generate_nginx_conf(self):
        """根据从sz文件读取的域名生成nginx.conf"""
        if not self.domain:
            print("⚠️  域名未配置，跳过nginx.conf生成")
            return False
        
        nginx_conf_path = self.project_dir / "nginx.conf"
        nginx_conf_content = f"""server {{
    listen 80;
    server_name {self.domain};

    # Redirect all HTTP requests to HTTPS
    return 301 https://$host$request_uri;
}}

server {{
    listen 443 ssl;
    server_name {self.domain};

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    location / {{
        proxy_pass http://{self.image_name}:{self.app_port};
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }}
}}
"""
        nginx_conf_path.write_text(nginx_conf_content, encoding="utf-8")
        print(f"✅ nginx.conf已生成 (域名: {self.domain})")
        return True
    
    def generate_compose_file(self):
        """生成docker-compose.yml，使用配置的端口映射"""
        compose_path = self.project_dir / "docker-compose.yml"
        compose_content = f"""services:
  crsp-mall:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: crsp-mall-container
    expose:
      - "8080"
    restart: unless-stopped
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m
    volumes:
      # Mount persistent volume for database to preserve data across restarts
      - mall-data:/data
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  nginx:
    image: nginx:alpine
    container_name: crsp-nginx
    ports:
      - "{self.http_port}:80"
      - "{self.ssl_port}:443"
    restart: unless-stopped
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - crsp-mall

volumes:
  mall-data:
    driver: local
"""
        compose_path.write_text(compose_content, encoding="utf-8")
        print(f"✅ docker-compose.yml已生成 (HTTP端口: {self.http_port}, HTTPS端口: {self.ssl_port})")
        return True
    
    def check_docker_installed(self):
        """检查Docker是否已安装"""
        print("🔍 检查Docker是否已安装...")
        try:
            result = subprocess.run(
                ["docker", "--version"],
                capture_output=True,
                text=True,
                check=True
            )
            print(f"✅ Docker已安装: {result.stdout.strip()}")
            return True
        except (subprocess.CalledProcessError, FileNotFoundError):
            print("❌ Docker未安装，请先安装Docker")
            print("   安装指南: https://docs.docker.com/get-docker/")
            return False
    
    def detect_project_type(self):
        """检测项目类型和依赖"""
        print("\n🔍 检测项目类型...")
        
        dependencies = {
            "java": False,
            "maven": False,
            "spring_boot": False,
            "thymeleaf": False
        }
        
        # 检查pom.xml (Maven项目)
        pom_file = self.project_dir / "pom.xml"
        if pom_file.exists():
            dependencies["maven"] = True
            print("✅ 检测到Maven项目 (pom.xml)")
            
            # 读取pom.xml检查依赖
            pom_content = pom_file.read_text(encoding="utf-8")
            
            if "spring-boot" in pom_content:
                dependencies["spring_boot"] = True
                dependencies["java"] = True
                print("✅ 检测到Spring Boot框架")
            
            if "thymeleaf" in pom_content:
                dependencies["thymeleaf"] = True
                print("✅ 检测到Thymeleaf模板引擎")
            
            # 检测Java版本
            if "<java.version>17</java.version>" in pom_content:
                dependencies["java_version"] = "17"
                print("✅ 检测到Java版本: 17")
            elif "<java.version>21</java.version>" in pom_content:
                dependencies["java_version"] = "21"
                print("✅ 检测到Java版本: 21")
            else:
                dependencies["java_version"] = "17"  # 默认使用17
                print("ℹ️  使用默认Java版本: 17")
        
        # 检查build.gradle (Gradle项目)
        gradle_file = self.project_dir / "build.gradle"
        if gradle_file.exists():
            dependencies["gradle"] = True
            print("✅ 检测到Gradle项目 (build.gradle)")
        
        # 检查package.json (Node.js项目)
        package_file = self.project_dir / "package.json"
        if package_file.exists():
            dependencies["nodejs"] = True
            print("✅ 检测到Node.js项目 (package.json)")
        
        # 检查requirements.txt (Python项目)
        requirements_file = self.project_dir / "requirements.txt"
        if requirements_file.exists():
            dependencies["python"] = True
            print("✅ 检测到Python项目 (requirements.txt)")
        
        return dependencies
    
    def generate_dockerfile(self, dependencies):
        """根据检测到的依赖生成Dockerfile"""
        print("\n📝 生成Dockerfile...")
        
        dockerfile_path = self.project_dir / "Dockerfile"
        
        # 针对Spring Boot Maven项目生成Dockerfile
        if dependencies.get("maven") and dependencies.get("spring_boot"):
            java_version = dependencies.get("java_version", "17")
            dockerfile_content = f"""# 多阶段构建 - 构建阶段
FROM maven:3.9-eclipse-temurin-{java_version} AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml先下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
COPY css ./css
COPY js ./js
COPY index.html .
RUN mvn clean package -DskipTests -B

# 运行阶段 - 使用更小的JRE镜像
FROM eclipse-temurin:{java_version}-jre

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /app/target/*.jar app.jar

# 暴露应用端口
EXPOSE {self.app_port}

# 设置环境变量
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
"""
        elif dependencies.get("nodejs"):
            dockerfile_content = f"""FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

EXPOSE {self.app_port}

CMD ["npm", "start"]
"""
        elif dependencies.get("python"):
            dockerfile_content = f"""FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE {self.app_port}

CMD ["python", "app.py"]
"""
        else:
            print("❌ 无法识别的项目类型")
            return False
        
        # 写入Dockerfile
        dockerfile_path.write_text(dockerfile_content, encoding="utf-8")
        print(f"✅ Dockerfile已生成: {dockerfile_path}")
        return True
    
    def generate_dockerignore(self):
        """生成.dockerignore文件"""
        print("\n📝 生成.dockerignore...")
        
        dockerignore_path = self.project_dir / ".dockerignore"
        dockerignore_content = """# Maven构建目录
target/

# IDE文件
.idea/
*.iml
.vscode/
*.swp
*.swo

# Git
.git/
.gitignore

# 日志文件
*.log
logs/

# 操作系统文件
.DS_Store
Thumbs.db

# 本地环境文件
.env.local
.env.*.local

# Docker相关
Dockerfile
docker-compose.yml
.dockerignore
"""
        dockerignore_path.write_text(dockerignore_content, encoding="utf-8")
        print(f"✅ .dockerignore已生成: {dockerignore_path}")
        return True
    
    def build_image(self):
        """构建Docker镜像"""
        print(f"\n🔨 构建Docker镜像: {self.image_name}...")
        print("   这可能需要几分钟时间，请耐心等待...\n")
        
        try:
            process = subprocess.Popen(
                ["docker", "build", "-t", self.image_name, "."],
                cwd=self.project_dir,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True
            )
            
            # 实时输出构建日志
            for line in process.stdout:
                print(f"   {line}", end="")
            
            process.wait()
            
            if process.returncode == 0:
                print(f"\n✅ Docker镜像构建成功: {self.image_name}")
                return True
            else:
                print(f"\n❌ Docker镜像构建失败，返回码: {process.returncode}")
                return False
                
        except Exception as e:
            print(f"❌ 构建过程出错: {e}")
            return False
    
    def stop_existing_container(self):
        """停止并删除已存在的容器"""
        print(f"\n🛑 检查并停止已存在的容器: {self.container_name}...")
        
        # 停止容器
        subprocess.run(
            ["docker", "stop", self.container_name],
            capture_output=True,
            text=True
        )
        
        # 删除容器
        subprocess.run(
            ["docker", "rm", self.container_name],
            capture_output=True,
            text=True
        )
        
        print("✅ 容器清理完成")
    
    def run_container(self):
        """运行Docker容器"""
        print(f"\n🚀 启动Docker容器...")
        print(f"   镜像: {self.image_name}")
        print(f"   容器名: {self.container_name}")
        print(f"   端口映射: {self.frontend_port} -> {self.app_port}")
        print(f"   数据卷: mall-data:/data (数据库持久化)")
        
        try:
            result = subprocess.run(
                [
                    "docker", "run",
                    "-d",  # 后台运行
                    "--name", self.container_name,
                    "-p", f"{self.frontend_port}:{self.app_port}",
                    "-v", "mall-data:/data",  # 挂载数据卷以持久化数据库
                    "--restart", "unless-stopped",  # 自动重启
                    self.image_name
                ],
                capture_output=True,
                text=True,
                check=True
            )
            
            container_id = result.stdout.strip()[:12]
            print(f"\n✅ 容器启动成功!")
            print(f"   容器ID: {container_id}")
            print(f"\n🌐 访问地址: http://localhost:{self.frontend_port}")
            return True
            
        except subprocess.CalledProcessError as e:
            print(f"❌ 容器启动失败: {e.stderr}")
            return False
    
    def show_container_logs(self):
        """显示容器日志"""
        print(f"\n📋 容器日志 (最近20行):")
        print("-" * 50)
        
        try:
            result = subprocess.run(
                ["docker", "logs", "--tail", "20", self.container_name],
                capture_output=True,
                text=True
            )
            print(result.stdout)
            if result.stderr:
                print(result.stderr)
        except Exception as e:
            print(f"无法获取日志: {e}")
    
    def check_container_status(self):
        """检查容器运行状态"""
        print(f"\n📊 容器状态:")
        
        try:
            result = subprocess.run(
                [
                    "docker", "ps",
                    "--filter", f"name={self.container_name}",
                    "--format", "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
                ],
                capture_output=True,
                text=True,
                check=True
            )
            print(result.stdout)
            return self.container_name in result.stdout
        except Exception as e:
            print(f"无法获取状态: {e}")
            return False
    
    def deploy_with_compose(self):
        """使用docker compose部署（含域名和SSL配置）"""
        print(f"\n🚀 使用docker compose部署...")
        
        # 停止旧容器
        subprocess.run(
            ["docker", "compose", "down"],
            cwd=self.project_dir,
            capture_output=True,
            text=True
        )
        
        # 构建并启动
        try:
            process = subprocess.Popen(
                ["docker", "compose", "up", "-d", "--build"],
                cwd=self.project_dir,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True
            )
            
            for line in process.stdout:
                print(f"   {line}", end="")
            
            process.wait()
            
            if process.returncode == 0:
                print("\n✅ docker compose部署成功!")
                return True
            else:
                print(f"\n❌ docker compose部署失败，返回码: {process.returncode}")
                return False
                
        except Exception as e:
            print(f"❌ 部署过程出错: {e}")
            return False
    
    def deploy(self):
        """执行完整的部署流程"""
        print("=" * 60)
        print("🐳 Docker自动部署脚本")
        print(f"   项目目录: {self.project_dir}")
        print(f"   前端端口: {self.frontend_port}")
        print(f"   DOCKER_API_VERSION: {os.environ.get('DOCKER_API_VERSION', 'not set')}")
        if self.domain:
            print(f"   域名: {self.domain}")
            print(f"   HTTP端口: {self.http_port}")
            print(f"   HTTPS端口: {self.ssl_port}")
        print("=" * 60)
        
        # 1. 检查Docker
        if not self.check_docker_installed():
            return False
        
        # 2. 检测项目依赖
        dependencies = self.detect_project_type()
        if not dependencies.get("maven") and not dependencies.get("nodejs") and not dependencies.get("python"):
            print("\n❌ 无法识别项目类型，请确保项目包含以下文件之一:")
            print("   - pom.xml (Maven/Java)")
            print("   - build.gradle (Gradle/Java)")
            print("   - package.json (Node.js)")
            print("   - requirements.txt (Python)")
            return False
        
        # 3. 生成Dockerfile
        if not self.generate_dockerfile(dependencies):
            return False
        
        # 4. 生成.dockerignore
        self.generate_dockerignore()
        
        # 5. 从sz文件配置SSL证书和nginx
        if self.domain and self.ssl_fullchain and self.ssl_privkey:
            print("\n🔒 配置域名和SSL...")
            self.setup_ssl()
            self.generate_nginx_conf()
            self.generate_compose_file()
            
            # 6. 使用docker compose部署（含nginx + SSL）
            if not self.deploy_with_compose():
                return False
        else:
            # 回退：无域名/SSL时使用直接docker运行
            # 5b. 构建镜像
            if not self.build_image():
                return False
            
            # 6b. 停止已存在的容器
            self.stop_existing_container()
            
            # 7b. 运行新容器
            if not self.run_container():
                return False
        
        # 8. 等待应用启动
        print("\n⏳ 等待应用启动 (约10秒)...")
        time.sleep(10)
        
        # 9. 检查状态
        self.check_container_status()
        
        # 10. 显示日志
        self.show_container_logs()
        
        print("\n" + "=" * 60)
        print("✅ 部署完成!")
        if self.domain:
            port_suffix = f":{self.ssl_port}" if self.ssl_port != self.STANDARD_HTTPS_PORT else ""
            print(f"🌐 请访问: https://{self.domain}{port_suffix}")
        else:
            print(f"🌐 请访问: http://localhost:{self.frontend_port}")
        print("=" * 60)
        
        return True


def print_usage():
    """打印使用说明"""
    print("""
Docker自动部署脚本使用说明
========================

用法:
    python docker_deploy.py [选项]

选项:
    -p, --port PORT         指定前端端口 (默认: 1000)
    -d, --dir DIR           指定项目目录 (默认: 当前目录)
    --ssl-port PORT         指定HTTPS端口 (默认: 8443，避免与其他服务的443冲突)
    --http-port PORT        指定HTTP端口 (默认: 80)
    -h, --help              显示此帮助信息

示例:
    python docker_deploy.py                    # 使用默认配置
    python docker_deploy.py -p 8000            # 使用端口8000
    python docker_deploy.py --ssl-port 443     # 使用标准443端口
    python docker_deploy.py --ssl-port 8443    # 使用8443端口避免冲突
    python docker_deploy.py -d /path/to/project -p 3000

管理命令:
    docker logs crsp-mall-container            # 查看日志
    docker stop crsp-mall-container            # 停止容器
    docker start crsp-mall-container           # 启动容器
    docker rm crsp-mall-container              # 删除容器
""")


def main():
    """主函数"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description="Docker自动部署脚本 - 自动检测依赖并打包运行",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    
    parser.add_argument(
        "-p", "--port",
        type=int,
        default=1000,
        help="前端端口 (默认: 1000)"
    )
    
    parser.add_argument(
        "-d", "--dir",
        type=str,
        default=None,
        help="项目目录 (默认: 当前目录)"
    )
    
    parser.add_argument(
        "--ssl-port",
        type=int,
        default=8443,
        help="HTTPS端口，宿主机映射 (默认: 8443，避免与其他服务的443冲突)"
    )
    
    parser.add_argument(
        "--http-port",
        type=int,
        default=80,
        help="HTTP端口，宿主机映射 (默认: 80)"
    )
    
    args = parser.parse_args()
    
    # 创建部署器并执行
    deployer = DockerDeployer(
        project_dir=args.dir,
        frontend_port=args.port,
        ssl_port=args.ssl_port,
        http_port=args.http_port
    )
    
    success = deployer.deploy()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
