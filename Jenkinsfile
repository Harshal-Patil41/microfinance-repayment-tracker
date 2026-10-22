// Declarative pipeline for the Microfinance Repayment Tracker (Tasks 7, 8, 10, 12).
// Works on a Windows or a Linux Jenkins agent. Agent needs: JDK 17, Maven, Chrome, Docker (+ compose plugin), Git.
// No secrets in this file: Docker Hub login uses the Jenkins credential with id "dockerhub-creds".

def run(String unixCmd, String winCmd) {
    if (isUnix()) { sh unixCmd } else { bat winCmd }
}

def startSeleniumApp(String port) {
    if (isUnix()) {
        sh "nohup java -jar target/tracker.war --spring.profiles.active=selenium --server.port=${port} > selenium-app.log 2>&1 & echo \$! > selenium-app.pid"
    } else {
        bat "powershell -NoProfile -Command \"\$p = Start-Process java -ArgumentList '-jar','target\\tracker.war','--spring.profiles.active=selenium','--server.port=${port}' -PassThru -RedirectStandardOutput selenium-app.log -RedirectStandardError selenium-app.err; \$p.Id | Out-File selenium-app.pid -Encoding ascii\""
    }
}

def stopSeleniumApp() {
    if (isUnix()) {
        sh 'if [ -f selenium-app.pid ]; then kill $(cat selenium-app.pid) || true; rm -f selenium-app.pid; fi'
    } else {
        bat 'powershell -NoProfile -Command "if (Test-Path selenium-app.pid) { Stop-Process -Id (Get-Content selenium-app.pid) -Force -ErrorAction SilentlyContinue; Remove-Item selenium-app.pid }"'
    }
}

def waitForHealth(String port) {
    if (isUnix()) {
        sh """
            for i in \$(seq 1 40); do
              if curl -fs http://localhost:${port}/actuator/health | grep -q '"status":"UP"'; then echo "healthy on ${port}"; exit 0; fi
              echo "waiting (\$i/40)"; sleep 3
            done
            echo "health check failed"; exit 1
        """
    } else {
        bat "powershell -NoProfile -Command \"for (\$i=0; \$i -lt 40; \$i++) { try { \$r = Invoke-RestMethod http://localhost:${port}/actuator/health; if (\$r.status -eq 'UP') { Write-Host 'healthy on ${port}'; exit 0 } } catch {}; Start-Sleep 3 }; Write-Host 'health check failed'; exit 1\""
    }
}

pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'test'], description: 'Target environment (dev = port 8082, test = port 8092)')
        booleanParam(name: 'DEPLOY', defaultValue: true, description: 'Deploy the container after all tests pass')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false, description: 'Push the image to Docker Hub (needs credential dockerhub-creds)')
        string(name: 'DOCKERHUB_USER', defaultValue: '', description: 'Docker Hub namespace (only when PUSH_IMAGE is ticked)')
    }

    environment {
        APP_NAME      = 'microfinance-tracker'
        IMAGE_TAG     = "1.0.${env.BUILD_NUMBER}"
        HOST_PORT     = "${params.DEPLOY_ENV == 'test' ? '8092' : '8082'}"
        SELENIUM_PORT = '8083'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    triggers {
        // Polling fallback (a laptop Jenkins is not reachable by GitHub webhooks). Delete if you use a webhook.
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                run('git log -1 --pretty=format:"%h %an %s"', 'git log -1 --pretty=format:"%h %an %s"')
            }
        }

        stage('Validate environment') {
            steps {
                run('java -version && mvn -version && docker --version && docker compose version',
                    'java -version && mvn -version && docker --version && docker compose version')
            }
        }

        stage('Build and unit tests') {
            steps { run('mvn -B clean test', 'mvn -B clean test') }
            post { always { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false } }
        }

        stage('Package WAR') {
            steps { run('mvn -B -DskipTests package', 'mvn -B -DskipTests package') }
            post { success { archiveArtifacts artifacts: 'target/tracker.war', fingerprint: true } }
        }

        stage('Selenium tests') {
            steps {
                startSeleniumApp(env.SELENIUM_PORT)
                waitForHealth(env.SELENIUM_PORT)
                run("mvn -B -f selenium-tests/pom.xml test -DbaseUrl=http://localhost:${env.SELENIUM_PORT} -Dheadless=true",
                    "mvn -B -f selenium-tests/pom.xml test -DbaseUrl=http://localhost:${env.SELENIUM_PORT} -Dheadless=true")
            }
            post {
                always {
                    junit testResults: 'selenium-tests/target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'selenium-tests/target/screenshots/*.png', allowEmptyArchive: true
                    stopSeleniumApp()
                }
            }
        }

        stage('Build Docker image') {
            steps {
                run('docker build --build-arg APP_VERSION=${IMAGE_TAG} -t ${APP_NAME}:${IMAGE_TAG} -t ${APP_NAME}:latest .',
                    'docker build --build-arg APP_VERSION=%IMAGE_TAG% -t %APP_NAME%:%IMAGE_TAG% -t %APP_NAME%:latest .')
            }
        }

        stage('Push image (optional)') {
            when { expression { params.PUSH_IMAGE && params.DOCKERHUB_USER?.trim() } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    run('''echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                           docker tag ${APP_NAME}:${IMAGE_TAG} ${DOCKERHUB_USER}/${APP_NAME}:${IMAGE_TAG}
                           docker push ${DOCKERHUB_USER}/${APP_NAME}:${IMAGE_TAG}
                           docker logout''',
                        '''echo %DH_PASS%| docker login -u %DH_USER% --password-stdin
                           docker tag %APP_NAME%:%IMAGE_TAG% %DOCKERHUB_USER%/%APP_NAME%:%IMAGE_TAG%
                           docker push %DOCKERHUB_USER%/%APP_NAME%:%IMAGE_TAG%
                           docker logout''')
                }
            }
        }

        stage('Deploy container') {
            when { expression { params.DEPLOY } }
            steps {
                script {
                    // Remember the running tag so a failed health check can roll back to it.
                    env.PREV_TAG = isUnix()
                        ? sh(returnStdout: true, script: "docker inspect --format '{{.Config.Image}}' ${env.APP_NAME} 2>/dev/null | awk -F: '{print \$NF}' || true").trim()
                        : bat(returnStdout: true, script: "@docker inspect --format \"{{.Config.Image}}\" ${env.APP_NAME} 2>nul || echo").trim().tokenize(':').last()
                    if (env.PREV_TAG == null || env.PREV_TAG.contains(' ') || env.PREV_TAG == 'echo') { env.PREV_TAG = '' }
                    echo "Previously deployed tag: '${env.PREV_TAG}'"
                }
                run('IMAGE_NAME=${APP_NAME} IMAGE_TAG=${IMAGE_TAG} HOST_PORT=${HOST_PORT} docker compose up -d --no-build',
                    'set IMAGE_NAME=%APP_NAME%&& set IMAGE_TAG=%IMAGE_TAG%&& set HOST_PORT=%HOST_PORT%&& docker compose up -d --no-build')
            }
        }

        stage('Health check') {
            when { expression { params.DEPLOY } }
            steps { waitForHealth(env.HOST_PORT) }
        }
    }

    post {
        failure {
            echo 'Build FAILED. Container logs (if any):'
            run('docker logs --tail 50 ${APP_NAME} || true', 'docker logs --tail 50 %APP_NAME% || echo no container')
            script {
                if (params.DEPLOY && env.PREV_TAG?.trim() && env.PREV_TAG != env.IMAGE_TAG) {
                    echo "Rolling back to ${env.PREV_TAG}"
                    run('IMAGE_NAME=${APP_NAME} IMAGE_TAG=${PREV_TAG} HOST_PORT=${HOST_PORT} docker compose up -d --no-build || true',
                        'set IMAGE_NAME=%APP_NAME%&& set IMAGE_TAG=%PREV_TAG%&& set HOST_PORT=%HOST_PORT%&& docker compose up -d --no-build')
                }
            }
        }
        success { echo "Release ${env.IMAGE_TAG} deployed to ${params.DEPLOY_ENV} on port ${env.HOST_PORT}." }
        cleanup { script { stopSeleniumApp() } }
    }
}
