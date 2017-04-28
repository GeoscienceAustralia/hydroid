cd /var/tmp

# update hydroid app
sudo kill -9 $(cat /usr/share/tomcat7/hydroid/hydroid.pid)
sudo mv /var/tmp/google-vision.json /usr/share/tomcat7/hydroid/google-vision.json
export GOOGLE_APPLICATION_CREDENTIALS=/usr/share/tomcat7/hydroid/google-vision.json
sudo cp /var/tmp/hydroid.jar /usr/share/tomcat7/hydroid/.
sudo java -jar /usr/share/tomcat7/hydroid/hydroid.jar > /dev/null 2> /dev/null < /dev/null &

# update tomcat-stanbol
sudo service tomcat7 stop
sudo rm -rf /usr/share/tomcat7/stanbol
#sudo rm -rf /usr/share/tomcat7/webapps/hydroid
#sudo cp /var/tmp/hydroid.war /usr/share/tomcat7/webapps/.
sudo service tomcat7 start
response=$(curl --output /dev/null --silent --fail -w %{http_code} http://localhost:8080/stanbol/enhancer/chain)
while [[ "${response}" != "200" ]]
do
    printf 'Waiting 2s for stanbol to restart... \n'
	sleep 2s
    response=$(curl --output /dev/null --silent --fail -w %{http_code} http://localhost:8080/stanbol/enhancer/chain)
done
defaultChainResponse=$(curl --output /dev/null --silent --fail -w %{http_code} http://localhost:8080/stanbol/entityhub)
printf 'Server last response was [%s]..\n' "$defaultChainResponse"
printf 'Stanbol ready, configuring...\n'
sleep 3s
# Copy GA.solrindex.zip to stanbol datafiles location
sudo cp /var/tmp/GA.solrindex.zip /usr/share/tomcat7/stanbol/datafiles/GA.solrindex.zip
printf 'Posting bundle...\n'
# post bundle to OSGi
sudo cp /var/tmp/org.apache.stanbol.data.site.GA-1.0.0.jar /usr/share/tomcat7/stanbol/fileinstall/org.apache.stanbol.data.site.GA-1.0.0.jar
sleep 2s
sudo cp /var/tmp/config/. /usr/share/tomcat7/stanbol/fileinstall/ -R

newPassword=$(uuidgen)
printf 'Lock Stanbol system console...'
# curl http://localhost:8080/stanbol/system/console/configMgr/org.apache.felix.webconsole.internal.servlet.OsgiManager -H 'Cookie: felix-webconsole-locale=en' -H 'Accept-Encoding: gzip, deflate' -H 'Accept-Language: en,en-AU;q=0.8' -H 'Authorization: Basic YWRtaW46YWRtaW4=' -H 'X-Requested-With: XMLHttpRequest' -H 'Connection: keep-alive' -H 'Pragma: no-cache' -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' -H 'Accept: */*' -H 'Cache-Control: no-cache' -H 'DNT: 1' --data 'apply=true&action=ajaxConfigManager&%24location=slinginstall%3Aorg.apache.felix.webconsole-4.2.8.jar&manager.root=%2Fsystem%2Fconsole&http.service.filter=&default.render=bundles&realm=OSGi+Management+Console&username=admin&password="${newPassword}"&category=Main&locale=&loglevel=2&plugins=org.apache.felix.webconsole.internal.compendium.LogServlet&plugins=org.apache.felix.webconsole.internal.configuration.ConfigManager&plugins=org.apache.felix.webconsole.internal.core.BundlesServlet&plugins=org.apache.felix.webconsole.internal.core.ServicesServlet&plugins=org.apache.felix.webconsole.internal.misc.LicenseServlet&plugins=org.apache.felix.webconsole.internal.system.VMStatPlugin&propertylist=manager.root%2Chttp.service.filter%2Cdefault.render%2Crealm%2Cusername%2Cpassword%2Ccategory%2Clocale%2Cloglevel%2Cplugins' --compressed

#Copy required AWS profile for API
cd /usr/share/tomcat7
sudo cp -R /home/ec2-user/.aws/ .
sudo chown -R tomcat:tomcat .aws/

