package net.java.kit.pca;

import org.knime.base.node.mine.pca.DialogComponentChoiceConfig;
import org.knime.base.node.mine.pca.SettingsModelPCADimensions;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

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
        
        addDialogComponent(new DialogComponentChoiceConfig(
        		new SettingsModelPCADimensions(
        			IPCANodeModel.CFGKEY_DIMENSION_PCA, IPCANodeModel.DEFAILT_DIMENSION_PCA_INT, 
        			IPCANodeModel.DEFAILT_DIMENSION_PCA_DOUBLE, false), 
        		false));
        
        addDialogComponent(new DialogComponentStringSelection(
        		new SettingsModelString(
        			IPCANodeModel.CFGKEY_WEIGHTING_SCHEME, 
        			IPCANodeModel.DEFAULT_WEIGHTING_SCHEME),
        		IPCANodeModel.CFGKEY_WEIGHTING_SCHEME, IPCANodeModel.m_weightingSchemes
        		));
        addDialogComponent(new DialogComponentColumnFilter2(
        		new SettingsModelColumnFilter2(
        			IPCANodeModel.CFGKEY_COLUMN_FILTER), 
        		0));
                    
    }
}

