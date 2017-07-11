# install
```
mvn clean package
unzip zk-util-bin.zip
cd zk-util
java -jar zk-util.jar
```
# commands

`zk-util>help`

help

* clear - Clears the console
* clone - Clone data between zookeepers
* cls - Clears the console
* create - create node
* date - Displays the local date and time
* delete - delete node and its all children
* exit - Exits the shell
* export - Export data with yml format
* file download - Download file from zookeeper
* file upload - Upload file to zookeeper
* get - Get node value
* help - List all commands usage
* import - Import data from yml format
* ls - list child nodes
* quit - Exits the shell
* script - Parses the specified resource file and executes its commands
* set - set value for exist value
* system properties - Shows the shell's properties
* version - Displays shell version

`zk-util>create --zk 192.168.5.99 --node /test/me --value hi`

create --zk 192.168.5.99 --node /test/me --value hi

Done

`zk-util>ls --zk 192.168.5.99 --node /test`

ls --zk 192.168.5.99 --node /test

[assembly.xml, me, author]

`zk-util>delete --zk 192.168.5.99 --node /test/me`

delete --zk 192.168.5.99 --node /test/me

Done

`zk-util>get --zk 192.168.5.99 --node /test/author`

get --zk 192.168.5.99 --node /test/author

wangyuxuan

`zk-util>clone --help`

clone --help

You should specify option (--source, --target, --node, --overwrite) for this command

`zk-util>clone --source 192.168.5.99 --target 192.168.6.16 --node /test`

clone --source 192.168.5.99 --target 192.168.6.16 --node /test
Sync node: /test/assembly.xml
Sync node: /test/author

Done

`zk-util>file upload --help`

file upload --help

You should specify option (--zk, --file, --zknode) for this command

`zk-util>file upload --zk 192.168.5.99 --file /tmp/test --zknode /test/tfile`

file upload --zk 192.168.5.99 --file /tmp/test --zknode /test/tfile
Uploading /tmp/test to 192.168.5.99/test/tfile

Done

`zk-util>file download --help`

file download --help

You should specify option (--zk, --file, --zknode) for this command

`zk-util>file download --zk 192.168.5.99 --zknode /test/tfile --file /tmp/dfile`

file download --zk 192.168.5.99 --zknode /test/tfile --file /tmp/dfile
Download 192.168.5.99/test/tfile to /tmp/dfile

Done

`zk-util>export --zk 192.168.5.99 --basePath /test --file /tmp/test.yml`

export --zk 192.168.5.99 --basePath /test --file /tmp/test.yml
Export node: /test
Export node: /test/assembly.xml
Export node: /test/tfile
Export node: /test/author
Done

`zk-util>import --zk 192.168.5.99 --basePath /test2 --file /tmp/test.yml`

import --zk 192.168.5.99 --basePath /test2 --file /tmp/test.yml
Create node: /test2
Create node: /test2/assembly.xml
Create node: /test2/tfile
Create node: /test2/author
Done
