#!/bin/bash 
JBOSS_HOME=./target/jboss-eap-6.0
SERVER_DIR=$JBOSS_HOME/standalone
SRC_DIR=./installs
SUPPORT_DIR=./support
EAP=jboss-eap-6.0.1.zip
EAP_VERSION=6.0.1
BRMS=brms-p-5.3.1.GA-deployable-ee6.zip
BRMS_VERSION=5.3.1
MVN_VERSION=5.3.1.BRMS

echo
echo Setting up the EAP + BRMS testing environment...
echo

command -v mvn -q >/dev/null 2>&1 || { echo >&2 "Maven is required but not installed yet... aborting."; exit 1; }

installPom() {
    mvn -q install:install-file -Dfile=../$SUPPORT_DIR/$2-$MVN_VERSION.pom.xml -DgroupId=$1 -DartifactId=$2 -Dversion=$MVN_VERSION -Dpackaging=pom;
}

installBinary() {
    unzip -q $2-$MVN_VERSION.jar META-INF/maven/$1/$2/pom.xml;
    mvn -q install:install-file -DpomFile=./META-INF/maven/$1/$2/pom.xml -Dfile=$2-$MVN_VERSION.jar -DgroupId=$1 -DartifactId=$2 -Dversion=$MVN_VERSION -Dpackaging=jar;
}

# Checks the availability of the downloads first before proceeding.
if [[ -x $SRC_DIR/$EAP || -L $SRC_DIR/$EAP ]]; then
	echo EAP sources are present...
	echo
else
	echo Need to download $EAP package from the Customer Support Portal 
	echo and place it in the $SRC_DIR directory to proceed...
	echo
	exit
fi

if [[ -x $SRC_DIR/$BRMS || -L $SRC_DIR/$BRMS ]]; then
	echo BRMS sources are present...
	echo
else
	echo Need to download $BRMS package from the Customer Support Portal 
	echo and place it in the $SRC_DIR directory to proceed...
	echo
	exit
fi

# Create the target directory if it does not already exist.
if [ ! -x target ]; then
	echo "  - creating the target directory..."
	echo
  mkdir target
else
	echo "  - detected target directory, moving on..."
	echo
fi

# Move the old JBoss instance, if it exists, to the OLD position.
if [ -x $JBOSS_HOME ]; then
	echo "  - existing JBoss Enterprise Application Platform $EAP_VERSION detected..."
	echo
	echo "  - moving existing JBoss Enterprise Application Platform $EAP_VERSION aside..."
	echo
  rm -rf $JBOSS_HOME.OLD
  mv $JBOSS_HOME $JBOSS_HOME.OLD
fi

# Unzip the JBoss EAP instance.
echo Unpacking JBoss Enterprise Application Platform $EAP_VERSION...
echo
unzip -q -d target $SRC_DIR/$EAP

# Unzip the required files from JBoss BRMS deployable.
echo Unpacking JBoss Enterprise BRMS $BRMS_VERSION...
echo

unzip -q $SRC_DIR/$BRMS jboss-brms-manager-ee6.zip 
echo "  - deploying JBoss Enterprise BRMS Manager WAR..."
echo
unzip -q -d $SERVER_DIR/deployments jboss-brms-manager-ee6.zip
rm jboss-brms-manager-ee6.zip

unzip -q $SRC_DIR/$BRMS jboss-jbpm-console-ee6.zip 
echo "  - deploying jBPM Console WARs..."
echo
unzip -q -d $SERVER_DIR/deployments jboss-jbpm-console-ee6.zip
rm jboss-jbpm-console-ee6.zip

echo Copy support files "for" enabling BRMS security and deployment...
echo
# Set permissions for BRMS.
cp $SUPPORT_DIR/standalone.xml $SERVER_DIR/configuration
cp $SUPPORT_DIR/brms-roles.properties $SERVER_DIR/configuration
cp $SUPPORT_DIR/brms-users.properties $SERVER_DIR/configuration
cp $SUPPORT_DIR/components.xml $SERVER_DIR/deployments/jboss-brms.war/WEB-INF
# Add groups & users for jBPM Human Tasks
cp $SUPPORT_DIR/web.xml $SERVER_DIR/deployments/jbpm-human-task.war/WEB-INF

# Enable deployments of BRMS WARs.
touch $SERVER_DIR/deployments/jboss-brms.war.dodeploy
touch $SERVER_DIR/deployments/business-central.war.dodeploy
touch $SERVER_DIR/deployments/business-central-server.war.dodeploy
touch $SERVER_DIR/deployments/designer.war.dodeploy
touch $SERVER_DIR/deployments/jbpm-human-task.war.dodeploy

echo Copy "test" framework and dependencies "for" example processes...
echo
# Copy test framework.
unzip -q -d $SERVER_DIR/deployments $SUPPORT_DIR/brms-testing.zip
# Enable deployment of the framework EAR.
touch $SERVER_DIR/deployments/brms-testing.ear.dodeploy
# Copy dependencies (model, work item handlers, dataproviders) for the example processes.
cp $SUPPORT_DIR/brms-testing-examples.jar $SERVER_DIR/deployments/brms-testing.ear/lib

echo
echo Installing the BRMS binaries into the Maven repository...
echo

unzip -q $SRC_DIR/$BRMS jboss-brms-engine.zip
unzip -q jboss-brms-engine.zip binaries/*
unzip -q $SRC_DIR/$BRMS jboss-jbpm-engine.zip
unzip -q -o -d ./binaries jboss-jbpm-engine.zip
cd binaries

echo Installing parent POMs...
echo
installPom org.drools droolsjbpm-parent
installPom org.drools droolsjbpm-knowledge
installPom org.drools drools-multiproject
installPom org.drools droolsjbpm-tools
installPom org.drools droolsjbpm-integration
installPom org.drools guvnor
installPom org.jbpm jbpm

echo Installing Drools binaries...
echo
# droolsjbpm-knowledge
installBinary org.drools knowledge-api
# drools-multiproject
installBinary org.drools drools-core
installBinary org.drools drools-compiler
installBinary org.drools drools-jsr94
installBinary org.drools drools-verifier
installBinary org.drools drools-persistence-jpa
installBinary org.drools drools-templates
installBinary org.drools drools-decisiontables
# droolsjbpm-tools
installBinary org.drools drools-ant
# droolsjbpm-integration
installBinary org.drools drools-camel
# guvnor
installBinary org.drools droolsjbpm-ide-common

echo Installing jBPM binaries...
echo
installBinary org.jbpm jbpm-flow
installBinary org.jbpm jbpm-flow-builder
installBinary org.jbpm jbpm-persistence-jpa
installBinary org.jbpm jbpm-bam
installBinary org.jbpm jbpm-bpmn2
installBinary org.jbpm jbpm-workitems
installBinary org.jbpm jbpm-human-task
installBinary org.jbpm jbpm-test

cd ..
rm -rf binaries
rm jboss-brms-engine.zip
rm jboss-jbpm-engine.zip

echo Installation of binaries "for" BRMS $MVN_VERSION complete.
echo

echo Readme file contents:
echo =====================================================
cat README.md
echo =====================================================
echo

echo "Setup BRMS $BRMS_VERSION (integrated with EAP $EAP_VERSION) Performance Test Framework Complete."
echo



