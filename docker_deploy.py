#!/usr/bin/env python3
"""
Docker自动部署脚本
自动检测项目依赖并打包到Docker环境运行
前端端口: 1000
"""

import os
import subprocess
import sys
import shutil
from pathlib import Path


class DockerDeployer:
    """Docker自动部署器"""
    
    def __init__(self, project_dir=None, frontend_port=1000):
        """
        初始化部署器
        
        Args:
            project_dir: 项目目录路径，默认为当前目录
            frontend_port: 前端端口，默认1000
        """
        self.project_dir = Path(project_dir) if project_dir else Path.cwd()
        self.frontend_port = frontend_port
        self.app_port = 8080  # Spring Boot默认端口
        self.image_name = "crsp-mall"
        self.container_name = "crsp-mall-container"
        
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
        
        try:
            result = subprocess.run(
                [
                    "docker", "run",
                    "-d",  # 后台运行
                    "--name", self.container_name,
                    "-p", f"{self.frontend_port}:{self.app_port}",
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
    
    def deploy(self):
        """执行完整的部署流程"""
        print("=" * 60)
        print("🐳 Docker自动部署脚本")
        print(f"   项目目录: {self.project_dir}")
        print(f"   前端端口: {self.frontend_port}")
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
        
        # 5. 构建镜像
        if not self.build_image():
            return False
        
        # 6. 停止已存在的容器
        self.stop_existing_container()
        
        # 7. 运行新容器
        if not self.run_container():
            return False
        
        # 8. 等待应用启动
        print("\n⏳ 等待应用启动 (约10秒)...")
        import time
        time.sleep(10)
        
        # 9. 检查状态
        self.check_container_status()
        
        # 10. 显示日志
        self.show_container_logs()
        
        print("\n" + "=" * 60)
        print("✅ 部署完成!")
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
    -p, --port PORT     指定前端端口 (默认: 1000)
    -d, --dir DIR       指定项目目录 (默认: 当前目录)
    -h, --help          显示此帮助信息

示例:
    python docker_deploy.py                    # 使用默认配置
    python docker_deploy.py -p 8000            # 使用端口8000
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
    
    args = parser.parse_args()
    
    # 创建部署器并执行
    deployer = DockerDeployer(
        project_dir=args.dir,
        frontend_port=args.port
    )
    
    success = deployer.deploy()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
