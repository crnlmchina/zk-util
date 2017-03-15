package ring.zk.util.command;

import java.io.File;
import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;

@Component
public class FileCommand implements CommandMarker {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileCommand.class);

	@CliCommand(value = "file upload", help = "Upload file to zookeeper")
	public String upload(
			@CliOption(key = { "zk",
					"zookeeper" }, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
			@CliOption(key = {
					"file" }, mandatory = true, help = "File location, e.g. /your/file/path") final String file,
			@CliOption(key = {
					"zknode" }, mandatory = true, help = "Zookeeper node for the file content, e.g. /zk/abc") final String zknode) {

		LOGGER.info("Uploading {} to {}{}", file, zk, zknode);

		final File f = new File(file);
		if (!f.exists()) {
			return "File not exists " + file;
		}
		if (!f.isFile()) {
			return "Invalid file " + file;
		}

		try {
			final byte[] bytes = Files.toByteArray(f);
			try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1000))) {
				client.start();
				try {
					if (client.checkExists().forPath(zknode) == null) {
						client.create().creatingParentsIfNeeded().forPath(zknode, bytes);
					} else {
						client.setData().forPath(zknode, bytes);
					}
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		} catch (IOException e) {
			return e.getMessage();
		}
		return "Done";
	}

	@CliCommand(value = "file download", help = "Download file from zookeeper")
	public String download(
			@CliOption(key = { "zk",
					"zookeeper" }, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
			@CliOption(key = {
					"file" }, mandatory = true, help = "File location, e.g. /your/file/path") final String file,
			@CliOption(key = {
					"zknode" }, mandatory = true, help = "Zookeeper node for the file content, e.g. /zk/abc") final String zknode) {

		LOGGER.info("Download {}{} to {}", zk, zknode, file);

		try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1000))) {
			client.start();
			try {
				if (client.checkExists().forPath(zknode) != null) {
					byte[] data = client.getData().forPath(zknode);
					Files.write(data, new File(file));
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		return "Done";
	}

}
