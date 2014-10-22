package net.java.kit.pca;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "IPCA" Node.
 * 
 *
 * @author 
 */
public class IPCANodeFactory 
        extends NodeFactory<IPCANodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public IPCANodeModel createNodeModel() {
        return new IPCANodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<IPCANodeModel> createNodeView(final int viewIndex,
            final IPCANodeModel nodeModel) {
        return new IPCANodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new IPCANodeDialog();
    }

}

