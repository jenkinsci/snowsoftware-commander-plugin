pipeline {
    environment {
        // Fix to keep Gradle Daemon running: https://wiki.jenkins.io/display/JENKINS/ProcessTreeKiller, https://snowsoftware.atlassian.net/browse/CMSC-18621
        JENKINS_NODE_COOKIE = 'dontKillMe'
    }
    agent {
        label 'pr_pipeline'
    }

    stages {
        stage('Init') {
            steps {
                sh 'env'
                sh 'git config --global user.email $GIT_AUTHOR_EMAIL && git config --global user.name $GIT_AUTHOR_NAME'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Release') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'b786d628-d2ee-478d-b94e-c269171e577f', passwordVariable: 'USER_PASSWORD', usernameVariable: 'USER_NAME')]) {
                    sh '''
                        # Mount Allspark share. It is used throughout the pipeline
                        root_path="//allspark.embotics.com/build"
                        version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

                        sudo mkdir -p -v /mnt/allspark_build
                        if ! grep -qs "$root_path /mnt/allspark_build" /proc/mounts; then
                            echo -e "Mounting $root_path\n"
                            # Using jenkins:jenkins here as this is the user running the build process
                            sudo mount -v -t cifs -o vers=1.0,gid=jenkins,uid=jenkins,domain=embotics,username=${USER_NAME},password=${USER_PASSWORD} ${root_path} /mnt/allspark_build
                        fi

                        mkdir -pv /mnt/allspark_build/nightly-jenkins-plugin/$version
                        [ -f /mnt/allspark_build/nightly-jenkins-plugin/$version/embotics-vcommander.hpi ] && echo "File already exists. Forgot to bump version?" && exit 1
                        cp -v target/embotics-vcommander.hpi /mnt/allspark_build/nightly-jenkins-plugin/$version
                    '''
                }
            }
        }
    }
}
