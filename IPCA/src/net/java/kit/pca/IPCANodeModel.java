package net.java.kit.pca;

import java.io.File;
import java.io.IOException;

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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import net.java.jinterval.matrixutils.ConverterMatrix;
import net.java.jinterval.pca.AbstractPCA;
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
        
    static final String CFGKEY_COUNT_PC = "Count principal components";
    static final int DEFAULT_COUNT_PC = 2;
    private final SettingsModelIntegerBounded countPC =
    	new SettingsModelIntegerBounded(CFGKEY_COUNT_PC, DEFAULT_COUNT_PC, 
    									0, Integer.MAX_VALUE);

    /**
     * Constructor for the node model.
     */
    protected IPCANodeModel() {
    	super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO do something here
        logger.info("Count of principal components:" + countPC.getIntValue());
        
        logger.info("Count of columns input datatable: " + 
        		inData[0].getDataTableSpec().getNumColumns());
        
        int countRows = inData[0].getRowCount();
        int countColumns = inData[0].getDataTableSpec().getNumColumns();
        
        Double[][] matrixDouble = new Double[countRows][countColumns];
        
        int numRow = 0;
        CloseableRowIterator iterator = inData[0].iterator();
        DataType currentCellType;
        while(iterator.hasNext()){
        	DataRow currentRow = iterator.next();
        	for (int numColumn=0; numColumn<countColumns; numColumn++){
        		currentCellType = currentRow.getCell(numColumn).getType();
        		
        		logger.info("["+numRow+"]["+numColumn+"]:" 
        				+currentCellType.toString()
        				+":"+inData[0].getDataTableSpec().getColumnSpec(numColumn).getType().toString()
        				);
        		if(currentCellType.equals(DoubleCell.TYPE)){
        			matrixDouble[numRow][numColumn] = ((DoubleCell)currentRow.getCell(numColumn)).getDoubleValue();
            	}else{
            		matrixDouble[numRow][numColumn] = (Double)((IntCell)currentRow.getCell(numColumn)).getDoubleValue();
            	}
        		System.out.print(matrixDouble[numRow][numColumn]+ "|");
        	}
        	numRow++;

            System.out.println();
        }
        
        Interval[][] intervalMatrix;
        intervalMatrix = ConverterMatrix.convertDoubleToInterval(matrixDouble);
        AbstractPCA.WeightingSchemes weightingScheme = AbstractPCA.WeightingSchemes.EqualWeight;
        CIPCA pca = new CIPCA (intervalMatrix, weightingScheme);
        pca.solve();
        
        Interval[][] scoresMatrixInterval = pca.getScoresMatrix();
        Double[][] scoresMatrixDouble = ConverterMatrix.convertIntervalToDouble(scoresMatrixInterval);
        
        
        DataColumnSpec[] allColSpecs = new DataColumnSpec[countColumns];
        
        // TODO Introduce new method
        for (int numPC = 0; numPC < countColumns/2; numPC++){
        	allColSpecs [numPC*2] = 
            		new DataColumnSpecCreator ("PC"+(numPC+1)+"_inf", DoubleCell.TYPE).createSpec();
        	allColSpecs [numPC*2+1] = 
            		new DataColumnSpecCreator ("PC"+(numPC+1)+"_sup", DoubleCell.TYPE).createSpec();
        }
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        
        
        // the execution context will provide us with storage capacity, in this
        // case a data container to which we will add rows sequentially
        // Note, this container can also handle arbitrary big data tables, it
        // will buffer to disc if necessary.
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        // let's add m_count rows to it
        
        for (int rowNumber = 0; rowNumber < inData[0].getRowCount(); rowNumber++){
        	RowKey key = new RowKey ("Data projected " + (rowNumber+1));
            DataCell[] cells = new DataCell [allColSpecs.length];
            for(int i=0; i<cells.length; i++){
            	cells[i] = new DoubleCell (scoresMatrixDouble[rowNumber][i]);
            }
            DataRow row = new DefaultRow (key, cells);
            container.addRowToTable(row);
            
        }
        /*
        for (int i = 0; i < m_count.getIntValue(); i++) {
            RowKey key = new RowKey("Row " + i);
            // the cells of the current row, the types of the cells must match
            // the column spec (see above)
            DataCell[] cells = new DataCell[3];
            cells[0] = new StringCell("String_" + i); 
            cells[1] = new DoubleCell(0.5 * i); 
            cells[2] = new IntCell(i);
            DataRow row = new DefaultRow(key, cells);
            container.addRowToTable(row);
            
            // check if the execution monitor was canceled
            exec.checkCanceled();
            exec.setProgress(i / (double)m_count.getIntValue(), 
                "Adding row " + i);
        }
        */
        // once we are done, we close the container and return its table
        container.close();
        
        
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
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
    	countPC.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        countPC.loadSettingsFrom(settings);

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

        countPC.validateSettings(settings);

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

