package net.java.kit.pca;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "IPCA" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class IPCANodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring IPCA node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected IPCANodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    IPCANodeModel.CFGKEY_COUNT_PC,
                    IPCANodeModel.DEFAULT_COUNT_PC,
                    0, Integer.MAX_VALUE),
                    "Count of principal components", 1, 5));
                    
    }
}

