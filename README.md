# Jenkins Coralogix Plugin

This plugin adds support for sending a job's console logs and 
push tags to [Coralogix](https://coralogix.com/).

## Installation

* Download the hpi file from GitHub releases

* Put the ``hpi`` file in the directory ``$JENKINS_HOME/plugins``

* Restart Jenkins

![Coralogix Plugin](docs/images/coralogix_plugin.png)

## Configuration

Go to ``Manage Jenkins``, open ``Configure system``,
find ``Coralogix`` section and configure your account private key
for sending system/audit/security logs:

![Coralogix Configuration](docs/images/coralogix_global_configuration.png)

## Credentials

Before usage you need to create ``Jenkins`` credentials with
``Coralogix`` private key for your team:

![Coralogix Configuration](docs/images/coralogix_credentials.png)

## Usage

This plugin provides sending build logs and tags pushing
both for ``Freestyle project`` and ``Pipelines``.

### Send logs

Send your build logs to Coralogix.

#### Freestyle project

Just check the ``Send build logs to Coralogix``,
select ``Private Key`` and provide ``Application name``:

![Coralogix Logs](docs/images/coralogix_send_logs.png)

#### Pipeline

This is the ``Groovy`` implementation:

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh "Hello world!"
            }
        }
    }
    post {
        always {
            coralogixSend privateKeyCredentialId: 'coralogix-production',
                          application: 'MyApp'
        }
    }
}
```

### Push tag

Push version tag to Coralogix.

#### Freestyle project

Add build step ``Push Coralogix tag`` and configure:

* **Private Key** - your Coralogix account private key
* **Tag name** - version tag name
* **Application name** - your application name
* **Subsystem names** - your subsystem names
* **Icon**(optional) - your own tag picture

![Coralogix Tag](docs/images/coralogix_push_tag.png)

### Pipeline

This is the ``Groovy`` representation of ``Push Coralogix tag`` build step:

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh "Hello world!"
            }
        }
    }
    post {
        success {
            coralogixTag privateKeyCredentialId: 'coralogix-production',
                         tag: '1.0.0',
                         application: 'MyApp',
                         subsystems: [
                            [name: 'staging'],
                            [name: 'production']
                         ],
                         icon: ''
        }
    }
}
```

## License

The Coralogix Plugin is licensed under the MIT License.