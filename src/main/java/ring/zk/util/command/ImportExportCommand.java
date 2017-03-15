package ring.zk.util.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.PathAndBytesable;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by yuxuanwang on 2017/3/15.
 */
@Component
public class ImportExportCommand implements CommandMarker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportCommand.class);

    @CliCommand(value = "export", help = "Export data with yml format")
    public String exportData(
            @CliOption(key = {"zk",
                    "zookeeper"}, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181") final String zk,
            @CliOption(key = {"basePath"}, mandatory = true, help = "Zookeeper base path, e.g. /test/abc") final String basePath,
            @CliOption(key = {"file"}, mandatory = true, help = "File export, e.g. /tmp/abc.yml") final String file) {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1000))) {
            client.start();
            try {
                final ZkNode root = readNode(client, basePath);
                if (root != null) {
                    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                    mapper.writeValue(new File(file), root);
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "Done";
    }

    private ZkNode readNode(CuratorFramework client, String path) throws Exception {
        LOGGER.info("Export node: {}", path);
        if (client.checkExists().forPath(path) != null) {
            byte[] data = client.getData().forPath(path);
            String value = data == null ? null : new String(data);
            ZkNode node = new ZkNode();
            node.setName(ZKPaths.getNodeFromPath(path));
            node.setValue(value);
            List<String> children = client.getChildren().forPath(path);
            if (children != null && !children.isEmpty()) {
                List<ZkNode> childNodes = Lists.newArrayList();
                for (String child : children) {
                    childNodes.add(readNode(client, ZKPaths.makePath(path, child)));
                }
                node.setChildren(childNodes);
            }

            return node;
        }
        return null;
    }

    @CliCommand(value = "import", help = "Import data from yml format")
    public String importData(
            @CliOption(key = {"zk",
                    "zookeeper"}, mandatory = true, help = "Zookeeper address, e.g. 127.0.0.1:2181")
            final String zk,
            @CliOption(key = {"basePath"}, mandatory = true, help = "Zookeeper base path, if ends with /, data with be imported to sub nodes, e.g. /test")
            final String basePath,
            @CliOption(key = {"file"}, mandatory = true, help = "File export, e.g. /tmp/abc.yml")
            final String file,
            @CliOption(key = {"clean"}, mandatory = false, unspecifiedDefaultValue = "false", help = "File export, e.g. /tmp/abc.yml")
            final boolean clean) {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            ZkNode root = mapper.readValue(new File(file), ZkNode.class);
            if (root != null) {
                try (CuratorFramework client = CuratorFrameworkFactory.newClient(zk, new RetryOneTime(1000))) {
                    client.start();
                    String parentPath = basePath;
                    if (!basePath.endsWith("/")) {
                        ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(basePath);
                        parentPath = pathAndNode.getPath();
                        root.setName(pathAndNode.getNode());
                    }
                    if(clean) {
                        final String cleanPath = ZKPaths.makePath(parentPath, root.getName());
                        LOGGER.info("Clean path: {}", cleanPath);
                        client.delete().deletingChildrenIfNeeded().forPath(cleanPath);
                    }
                    saveNode(client, parentPath, root);
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        } catch (IOException e) {
            return e.getMessage();
        }
        return "Done";
    }

    private void saveNode(CuratorFramework client, String parentPath, ZkNode node) throws Exception {
        final PathAndBytesable<String> pathAndBytesable = client.create().creatingParentsIfNeeded();
        final String path = ZKPaths.makePath(parentPath, node.getName());
        LOGGER.info("Create node: {}", path);
        if (node.getValue() == null) {
            pathAndBytesable.forPath(path);
        } else {
            pathAndBytesable.forPath(path, node.getValue().getBytes());
        }
        List<ZkNode> children = node.getChildren();
        if (children != null) {
            for (ZkNode child : children) {
                saveNode(client, path, child);
            }
        }
    }
}
