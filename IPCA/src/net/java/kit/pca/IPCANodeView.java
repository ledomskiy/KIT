package net.java.kit.pca;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "IPCA" Node.
 * 
 *
 * @author 
 */
public class IPCANodeView extends NodeView<IPCANodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link IPCANodeModel})
     */
    protected IPCANodeView(final IPCANodeModel nodeModel) {
        super(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        IPCANodeModel nodeModel = 
            (IPCANodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

    }

}

