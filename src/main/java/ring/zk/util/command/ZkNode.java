package ring.zk.util.command;

import java.util.List;

/**
 * Created by yuxuanwang on 2017/3/15.
 */
public class ZkNode {

    private String name;
    private String value;
    private List<ZkNode> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ZkNode> getChildren() {
        return children;
    }

    public void setChildren(List<ZkNode> children) {
        this.children = children;
    }
}
