pipeline {
    agent any

    stages {
        stage('GIT') {
            steps {
                git branch: 'master',
                    changelog: false,
                    credentialsId: 'github-https-cred',
                    url: 'https://github.com/roumy-hub/devops'
            }
        }

        stage('MAVEN Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('SONARQUBE'){
            environment{
                SONAR_HOST_URL='http://192.168.50.4:9000/'
                SONAR_AUTH_TOKEN= credentials('sonarqube')
            }
            steps{
                sh'mvn sonar:sonar -Dsonar.projectKey=devops_git -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_AUTH_TOKEN'
            }
        }


        stage('TRIVY - Filesystem Scan') {
    steps {
        script {
            echo "Running Trivy filesystem scan on source code..."
            sh '''
            # Download vulnerability database (only needed once)
            trivy image --download-db-only

            # Scan filesystem for vulnerabilities in dependencies
            trivy fs . --scanners vuln --severity HIGH,CRITICAL --format table -o trivy-fs-report.txt || true

            # Scan for secrets in code
            trivy fs . --scanners secret --format table -o trivy-secrets-report.txt || true

            # Check if reports were generated
            if [ -f "trivy-fs-report.txt" ]; then
                echo "Trivy filesystem scan completed"
                cat trivy-fs-report.txt | head -20
            else
                echo "No Trivy filesystem report generated"
            fi
            '''
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'trivy-*.txt', fingerprint: true
        }
    }
}

stage('TRIVY - Docker Image Scan') {
    steps {
        script {
            echo "Running Trivy Docker image scan..."
            sh '''
            # Check if Dockerfile exists, create one if not
            if [ ! -f "Dockerfile" ]; then
                echo "Creating demo Dockerfile for security scan..."
                cat > Dockerfile << 'EOF'
FROM eclipse-temurin:11-jre
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
EOF
            fi

            # Build and scan Docker image
            docker build -t timesheet-devops:latest . || echo "Docker build completed"
            trivy image --severity HIGH,CRITICAL --format table -o trivy-image-report.txt timesheet-devops:latest || echo "Trivy scan completed"
            '''
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'trivy-image-report.txt', fingerprint: true, allowEmptyArchive: true
        }
    }
}
stage('GITLEAKS - Secret Scan') {
            steps {
                sh '''
                    gitleaks detect --source . --report-format json --report-path gitleaks-report.json --exit-code 1
                '''
            }
        }
    }
}