package ring.zk.util.command;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * Clone data between zookeepers
 * 
 * @author yuxuanwang
 *
 */
@Component
public class SyncCommand implements CommandMarker {

	private static final Logger LOGGER = LoggerFactory.getLogger(SyncCommand.class);

	@CliCommand(value = "clone", help = "Clone data between zookeepers")
	public String clone(
			@CliOption(key = {
					"source" }, mandatory = true, help = "Zookeeper source address, e.g. 127.0.0.1:2181") final String source,
			@CliOption(key = {
					"target" }, mandatory = true, help = "Zookeeper target address, e.g. 127.0.0.1:2181") final String target,
			@CliOption(key = {
					"node" }, mandatory = true, help = "The node to copy, e.g. /test/node") final String node,
			@CliOption(key = {
					"overwrite" }, mandatory = false, unspecifiedDefaultValue = "false", help = "If overwrite exist value") final boolean overwrite) {
		try (CuratorFramework sourceClient = CuratorFrameworkFactory.newClient(source, new RetryOneTime(1000))) {
			sourceClient.start();
			try (CuratorFramework targetClient = CuratorFrameworkFactory.newClient(target, new RetryOneTime(1000))) {
				targetClient.start();
				try {
					Stat stat = sourceClient.checkExists().forPath(node);
					if (stat != null) {

						if (targetClient.checkExists().forPath(node) == null) {
							System.out.println("Create root node: " + node);
							targetClient.create().creatingParentsIfNeeded().forPath(node,
									sourceClient.getData().forPath(node));
						}

						syncChildren(node, sourceClient, targetClient, overwrite);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return "Done";
	}

	private void syncChildren(String rootNode, CuratorFramework sourceClient, CuratorFramework targetClient,
			boolean overwrite) throws Exception {
		List<String> children = sourceClient.getChildren().forPath(rootNode);
		if (children != null) {
			for (String child : children) {
				String path = ZKPaths.makePath(rootNode, child);
				LOGGER.info("Sync node: " + path);
				byte[] data = sourceClient.getData().forPath(path);
				Stat stat = targetClient.checkExists().forPath(path);
				if (stat == null) {
					targetClient.create().forPath(path, data);
				} else if (overwrite) {
					targetClient.setData().forPath(path, data);
				}

				syncChildren(path, sourceClient, targetClient, overwrite);
			}
		}
	}

}
