package net.java.kit.pca;

import java.io.File;
import java.io.IOException;

import org.knime.base.node.mine.pca.SettingsModelPCADimensions;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import net.java.jinterval.matrixutils.ConverterMatrix;
import net.java.jinterval.pca.AbstractPCA;
import net.java.jinterval.pca.CPCA;
import net.java.jinterval.pca.VPCA;
import net.java.jinterval.pca.CIPCA;
import net.java.jinterval.interval.Interval;

/**
 * This is the model implementation of IPCA.
 * 
 *
 * @author 
 */
public class IPCANodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(IPCANodeModel.class);
    
    static final String CFGKEY_PCA_METHOD = "PCA method";
    static final String DEFAULT_PCA_METHOD = "CIPCA";
    static final String[] m_pcaMethods = 
    	{"CPCA", "VPCA","CIPCA"};
    private final SettingsModelString m_pcaMethod = 
        	new SettingsModelString(CFGKEY_PCA_METHOD, DEFAULT_PCA_METHOD);
    
        
    static final String CFGKEY_DIMENSION_PCA = "PCA dimensions";
    static final int DEFAILT_DIMENSION_PCA_INT = 2;
    static final double DEFAILT_DIMENSION_PCA_DOUBLE = 80.0;
    private final SettingsModelPCADimensions m_dimensionsPCA = 
    	new SettingsModelPCADimensions(
    		CFGKEY_DIMENSION_PCA, DEFAILT_DIMENSION_PCA_INT, 
    		DEFAILT_DIMENSION_PCA_DOUBLE, false);
    
    
    static final String CFGKEY_WEIGHTING_SCHEME = "Weighting scheme";
    static final String DEFAULT_WEIGHTING_SCHEME = "EqualWeight";
    static final String[] m_weightingSchemes = 
    	{"EqualWeight", "ProportionalVolume","InverselyProportionalVolume"};
    
    private final SettingsModelString m_weightingScheme = 
    	new SettingsModelString(CFGKEY_WEIGHTING_SCHEME, DEFAULT_WEIGHTING_SCHEME);
    
    static final String CFGKEY_COLUMN_FILTER = "Filter of columns";
    private final SettingsModelColumnFilter2 m_columnFilter = 
    	new SettingsModelColumnFilter2(CFGKEY_COLUMN_FILTER);

    /**
     * Constructor for the node model.
     */
    protected IPCANodeModel() {
    	super(1, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Count of columns input datatable: " + 
        		inData[0].getDataTableSpec().getNumColumns());
        
        int countRows = inData[0].getRowCount();
        int countColumns = inData[0].getDataTableSpec().getNumColumns();
        
        Double[][] matrixDouble = new Double[countRows][countColumns];
        RowKey[] rowKeys = new RowKey [inData[0].getRowCount()];
        
        int numRow = 0;
        CloseableRowIterator iterator = inData[0].iterator();
        DataType currentCellType;
        while(iterator.hasNext()){
        	DataRow currentRow = iterator.next();
        	for (int numColumn=0; numColumn<countColumns; numColumn++){
        		
        		currentCellType = currentRow.getCell(numColumn).getType();
        		
        		if(currentCellType.equals(DoubleCell.TYPE)){
        			matrixDouble[numRow][numColumn] = ((DoubleCell)currentRow.getCell(numColumn)).getDoubleValue();
            	}else{
            		matrixDouble[numRow][numColumn] = (Double)((IntCell)currentRow.getCell(numColumn)).getDoubleValue();
            	}
        		System.out.print(matrixDouble[numRow][numColumn]+ "|");
        	}
        	rowKeys [numRow] = currentRow.getKey();
        	numRow++;

            System.out.println();
        }
        
        
        Interval[][] intervalMatrix;
        intervalMatrix = ConverterMatrix.convertDoubleToInterval(matrixDouble);
        int countPC;
        double cumulativeContributionRate;
        AbstractPCA.WeightingSchemes weightingScheme
        	= AbstractPCA.WeightingSchemes.valueOf(m_weightingScheme.getStringValue()); 
        AbstractPCA pca = null;
        
        logger.info("PCA Method = " + m_pcaMethod.getStringValue());
        logger.info("WeightingScheme = " + weightingScheme.toString());
        if (m_dimensionsPCA.getDimensionsSelected()){
        	countPC = m_dimensionsPCA.getDimensions();
        	logger.info("countPC = " + countPC);
        	switch (m_pcaMethod.getStringValue()){
        		case "CPCA": pca = new CPCA (intervalMatrix, countPC, weightingScheme);
        					 break;
        		case "VPCA": pca = new VPCA (intervalMatrix, countPC, weightingScheme);
	             		     break;
        		case "CIPCA": pca = new CIPCA (intervalMatrix, countPC, weightingScheme);
	                          break;
        	}
        	
        }else{
        	cumulativeContributionRate = m_dimensionsPCA.getMinQuality();
        	logger.info("cumulativeContributionRate = " + cumulativeContributionRate);
        	switch (m_pcaMethod.getStringValue()){
	    		case "CPCA": pca = new CPCA (intervalMatrix, cumulativeContributionRate, weightingScheme);
	    		             break;
	    		case "VPCA": pca = new VPCA (intervalMatrix, cumulativeContributionRate, weightingScheme);
	             	         break;
	    		case "CIPCA": pca = new CIPCA (intervalMatrix, cumulativeContributionRate, weightingScheme);
	                          break;
        	}
        }
        
        pca.solve();
        
        Interval[][] scoresMatrixInterval = pca.getScoresMatrix();
        Double[][] scoresMatrixDouble = ConverterMatrix.convertIntervalToDouble(scoresMatrixInterval);
        
        int countColumnScoresMatrix = scoresMatrixDouble[0].length;
        DataColumnSpec[] scoresMatrixSpecs = new DataColumnSpec[countColumnScoresMatrix];
        
        // TODO Introduce new method
        for (int numPC = 0; numPC < countColumnScoresMatrix/2; numPC++){
        	scoresMatrixSpecs [numPC*2] = 
            		new DataColumnSpecCreator ("PC"+(numPC+1)+"_inf", DoubleCell.TYPE).createSpec();
        	scoresMatrixSpecs [numPC*2+1] = 
            		new DataColumnSpecCreator ("PC"+(numPC+1)+"_sup", DoubleCell.TYPE).createSpec();
        }
        DataTableSpec scoresOutputSpec = new DataTableSpec(scoresMatrixSpecs);
        
        
        BufferedDataContainer containerScores = exec.createDataContainer(scoresOutputSpec);
        
        for (int rowNumber = 0; rowNumber < inData[0].getRowCount(); rowNumber++){
        	RowKey key = rowKeys[rowNumber];
            DataCell[] cells = new DataCell [countColumnScoresMatrix];
            for(int i=0; i<cells.length; i++){
            	cells[i] = new DoubleCell (scoresMatrixDouble[rowNumber][i]);
            }
            DataRow row = new DefaultRow (key, cells);
            containerScores.addRowToTable(row);
            
        }
        containerScores.close();
        BufferedDataTable scoresDataTable = containerScores.getTable();
        
        
        double[][] loadingMatrix = pca.getLoadingsMatrix();
        int countColumnLoadingMatrix = loadingMatrix[0].length;
        DataColumnSpec[] loadingMatrixSpecs = new DataColumnSpec[countColumnLoadingMatrix];
        
        
        for (int numPC = 0; numPC < countColumnLoadingMatrix; numPC++){
        	loadingMatrixSpecs [numPC] = 
            		new DataColumnSpecCreator ("PC"+(numPC+1), DoubleCell.TYPE).createSpec();
        }
        DataTableSpec loadingOutputSpec = new DataTableSpec(loadingMatrixSpecs);
        
        
        BufferedDataContainer containerLoadings = exec.createDataContainer(loadingOutputSpec);
        
        for (int rowNumber = 0; rowNumber < loadingMatrix.length; rowNumber++){
        	RowKey key = new RowKey (inData[0].getDataTableSpec().getColumnSpec(rowNumber*2).getName());
            DataCell[] cells = new DataCell [countColumnLoadingMatrix];
            for(int i=0; i<cells.length; i++){
            	cells[i] = new DoubleCell (loadingMatrix[rowNumber][i]);
            }
            DataRow row = new DefaultRow (key, cells);
            containerLoadings.addRowToTable(row);
            
        }
        containerLoadings.close();
        BufferedDataTable loadingsDataTable = containerLoadings.getTable();
        
        
        
        double[] contributionRates = pca.getContributionRates();
        DataColumnSpec[] contributionRatesSpecs = new DataColumnSpec[1];
        contributionRatesSpecs[0] = new DataColumnSpecCreator ("Contribution", DoubleCell.TYPE).createSpec();
        
        DataTableSpec contributionOutputSpec = new DataTableSpec(contributionRatesSpecs);
        
        
        BufferedDataContainer containerContributions = exec.createDataContainer(contributionOutputSpec);
        
        for (int rowNumber = 0; rowNumber < contributionRates.length; rowNumber++){
        	RowKey key = new RowKey ("PC"+(rowNumber+1));
            DataCell[] cells = new DataCell [1];
            cells[0] = new DoubleCell (contributionRates[rowNumber]);
            DataRow row = new DefaultRow (key, cells);
            containerContributions.addRowToTable(row);
        }
        
        containerContributions.close();
        BufferedDataTable contributionsDataTable = containerContributions.getTable();
        
        
        
        return new BufferedDataTable[]{scoresDataTable, loadingsDataTable, contributionsDataTable};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_pcaMethod.saveSettingsTo(settings);
    	m_columnFilter.saveSettingsTo(settings);
    	m_dimensionsPCA.saveSettingsTo(settings);
    	m_weightingScheme.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_pcaMethod.loadSettingsFrom(settings);
        m_columnFilter.loadSettingsFrom(settings);
        m_dimensionsPCA.loadSettingsFrom(settings);
        m_weightingScheme.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO check if the settings could be applied to our model
        // e.g. if the count is in a certain range (which is ensured by the
        // SettingsModel).
        // Do not actually set any values of any member variables.
    	m_pcaMethod.validateSettings(settings);
        m_columnFilter.validateSettings(settings);
        m_dimensionsPCA.validateSettings(settings);
        m_weightingScheme.validateSettings(settings);

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }

}

