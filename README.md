 {jenkins-plugin-info:embotics-Commander-plugin}

Embotics provides a plugin for Jenkins automation servers (Jenkins
version 1.651.3 or greater). The Commander Jenkins plugin allows you to
configure a Jenkins build job to request virtual services and run
command workflows in a Commander instance (version 7.0.0 or greater) as
part of building, deploying and installing a project.

Using the Commander Jenkins plugin, you can add any of the following
build steps to freestyle projects:

-   request virtual services
-   run command workflows
-   wait steps to ensure that requests or workflows have sufficient time
    to complete

The Commander plugin enables more effective continuous integration and
delivery for improved DevOps capabilities. Using the Commander plugin
allows you to quickly add Commander build steps — you don't need to
create lengthy and complex scripts to include in your Jenkins job.

For example, with the help of the Commander plugin you can create a
Jenkins job with Commander build steps that each day would delete the
test virtual machine from the previous day, submit a service request to
deploy a new VM, and install the most recent build of your software on
the new VM.

Embotics Commander multi-cloud management platform is the fastest and
easiest way to automate provisioning, enforce governance, and enable
self-service IT across virtualized, private and public cloud
infrastructures.

<https://www.embotics.com/>

2019 Embotics Corporation

Multi-cloud Management Platform \| Embotics Commander

  

### Version History

#### Version 1.9 (Nov 20, 2019)

-   Bugfixes and rebranding from vCommander to Commander

#### Version 1.8 (Jan 11, 2019)

-   Bugfix: rest v.3 bugfix for error passthrough

#### Version 1.7 (Sept 28, 2018)

-   Bugfix: fixed typo in the help for 'wait for service request
    completion'

#### Version 1.6 (July 26, 2018)

-   Bugfix: removed port number from help example as it's not needed
    anymore

#### Version 1.5 (July 25, 2018)

-   Added new Embotics logo to plugin

#### Version 1.4 (May 29th, 2018)

-   fixed issue with wiki link in pom.xml
-   readme spacing

#### Version 1.3 (May 29th, 2018)

-   testing an automated release cycle with an internal version of
    jenkins and maven

#### Version 1.2 (May 28th, 2018) - Initial production release

-   first stable release of Embotics' Commander Plugin for Jenkins
-   compatible with all versions of Commander 7.0 GA +
