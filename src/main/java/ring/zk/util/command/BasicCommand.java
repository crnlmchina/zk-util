package ring.zk.util.command;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * Basic ops
 * 
 * @author yuxuanwang
 *
 */
@Component
public class BasicCommand implements CommandMarker {

	@CliCommand(value = "get", help = "Get node value")
	public String get(
			@CliOption(key = { "zk",
					"zookeeper" }, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
			@CliOption(key = { "node" }, mandatory = true, help = "Zookeeper node, e.g. /test/abc") final String node) {
		try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1))) {
			client.start();
			try {
				if (client.checkExists().forPath(node) != null) {
					byte[] data = client.getData().forPath(node);
					if (data == null) {
						return null;
					}
					return new String(data);
				} else {
					return "Node not exists";
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}
	}

	@CliCommand(value = "ls", help = "list child nodes")
	public String ls(
			@CliOption(key = { "zk",
					"zookeeper" }, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
			@CliOption(key = { "node" }, mandatory = true, help = "Zookeeper node, e.g. /test/abc") final String node) {
		try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1))) {
			client.start();
			try {
				if (client.checkExists().forPath(node) != null) {
					List<String> children = client.getChildren().forPath(node);
					if (children == null) {
						return null;
					}
					return children.toString();
				} else {
					return "Node not exists";
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}
	}

	@CliCommand(value = "delete", help = "delete node and its all children")
	public String delete(
			@CliOption(key = { "zk",
					"zookeeper" }, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
			@CliOption(key = { "node" }, mandatory = true, help = "Zookeeper node, e.g. /test/abc") final String node) {
		try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1))) {
			client.start();
			try {
				if (client.checkExists().forPath(node) != null) {
					client.delete().deletingChildrenIfNeeded().forPath(node);
					return "Done";
				} else {
					return "Node not exists";
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}
	}

	@CliCommand(value = "set", help = "set value for exist value")
	public String set(
			@CliOption(key = { "zk",
					"zookeeper" }, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
			@CliOption(key = { "node" }, mandatory = true, help = "Zookeeper node, e.g. /test/abc") final String node,
			@CliOption(key = { "value" }, mandatory = true, help = "node value") final String value) {
		try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1))) {
			client.start();
			try {
				client.setData().forPath(node, value.getBytes());
			} catch (Exception e) {
				return e.getMessage();
			}
		}
		return "Done";
	}

	@CliCommand(value = "create", help = "create node")
	public String create(
			@CliOption(key = { "zk",
					"zookeeper" }, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
			@CliOption(key = { "node" }, mandatory = true, help = "Zookeeper node, e.g. /test/abc") final String node,
			@CliOption(key = { "value" }, mandatory = true, help = "node value") final String value) {
		try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1))) {
			client.start();
			try {
				client.create().creatingParentsIfNeeded().forPath(node, value.getBytes());
			} catch (Exception e) {
				return e.getMessage();
			}
		}
		return "Done";
	}

}
