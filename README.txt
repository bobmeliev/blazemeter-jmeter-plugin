                          HOW TO USE

1.Open jmeter.properties. Add the following entries to the end of the file
#-----------------------------------------------------------------------------
# Blazemeter properties
#-----------------------------------------------------------------------------
#Blazemeter production environment
blazemeter.url=https://a.blazemeter.com
blazemeter.user_key=<your user key>
#
blazemeter.console_write=true
blazemeter.log_write=true
blazemeter.debug_enabled=false
server_log_file=jmeter-server.log

2.(For WINDOWS)
  Open jmeter-server.bat
  Find the string:
       call jmeter -s -j jmeter-server.log %JMETER_CMD_LINE_ARGS%
  Replace "jmeter-server.log" with the value of "server_log_file"

3.(For *NIX)
  Open jmeter-server
  Find the string:
        -s -j jmeter-server.log
  Replace "jmeter-server.log" with the value of "server_log_file"

For more info please visit our community page:
http://community.blazemeter.com/knowledgebase/articles/83191-blazemeter-plugin-to-jmeter