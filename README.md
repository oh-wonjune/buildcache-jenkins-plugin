# buildcache-jenkins-plugin



    pipeline {
        agent any
    
        tools {
            nodejs 'your nodejs name'
        }

        stages {
            stage('Checkout') {
                steps {
                    git url: 'your git url name'
                }
            }
            stage('Build') {
                steps {
                    wrap([$class: 'CachedBuildWrapper', cacheServerAddress: 'your ServerAddress', cacheServerPort: your ServerPort]) {
                        sh 'npm ci'
                        // your npm build step
                    }
                }
            }
        }
    }
