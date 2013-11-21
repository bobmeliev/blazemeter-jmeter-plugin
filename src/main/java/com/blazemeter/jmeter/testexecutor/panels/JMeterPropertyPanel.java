package com.blazemeter.jmeter.testexecutor.panels;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testinfo.TestInfo;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.gui.GuiUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/14/13
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class JMeterPropertyPanel extends AbstractConfigGui
        implements ActionListener, UnsharedComponent {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "name"; // $NON-NLS-1$

    private static final String VALUE = "value"; // $NON-NLS-1$

    private static final String ADD = "add"; // $NON-NLS-1$

    private static final String DELETE = "delete"; // $NON-NLS-1$

    private static final String LOAD = "load"; // $NON-NLS-1$

    /**
     * The table containing the list of arguments.
     */
    private transient JTable table;

    /**
     * The model for the arguments table.
     */
//    protected transient ObjectTableModel tableModel;
    private transient PowerTableModel tableModel;

    /**
     * A button for adding new arguments to the table.
     */
    private JButton addButton;

    /**
     * A button for removing arguments from the table.
     */
    private JButton deleteButton;

    /**
     * A button for loading properties from the jmeter.properties.
     */
    private JButton loadButton;

    public JMeterPropertyPanel() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "property_visualiser_title"; // $NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent action) {
        String command = action.getActionCommand();
        if (ADD.equals(command)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(table);
            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }


            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            table.setRowSelectionInterval(rowToSelect, rowToSelect);

//            return;
        }
        if (DELETE.equals(command)) {
            if (tableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                if (table.isEditing()) {
                    TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(),
                            table.getEditingColumn());
                    cellEditor.cancelCellEditing();
                }

                int rowSelected = table.getSelectedRow();

                if (rowSelected != -1) {
                    tableModel.removeRow(rowSelected);
                    tableModel.fireTableDataChanged();

                    // Disable the DELETE and SAVE buttons if no rows remaining
                    // after delete.
                    if (tableModel.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    }

                    // Table still contains one or more rows, so highlight
                    // (select) the appropriate one.
                    else {
                        int rowToSelect = rowSelected;

                        if (rowSelected >= tableModel.getRowCount()) {
                            rowToSelect = rowSelected - 1;
                        }

                        table.setRowSelectionInterval(rowToSelect, rowToSelect);

                    }
                }
            }
        }
        if (LOAD.equals(command)) {
            setUpData();
        }
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.getTestInfo().setJmeterProperties(getData());
    }

    @Override
    public TestElement createTestElement() {
        TestElement el = new ConfigTestElement();
        modifyTestElement(el);
        return el;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        setUpData();
    }

    private void setUpData() {
        tableModel.clearData();
        Properties p = null;
        p = JMeterUtils.getJMeterProperties();
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.getTestInfo().setJmeterProperties(p);
        if (p == null) {
            return;
        }
        Set<Map.Entry<Object, Object>> s = p.entrySet();
        ArrayList<Map.Entry<Object, Object>> al = new ArrayList<Map.Entry<Object, Object>>(s);
        Collections.sort(al, new Comparator<Map.Entry<Object, Object>>() {
            @Override
            public int compare(Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) {
                String m1, m2;
                m1 = (String) o1.getKey();
                m2 = (String) o2.getKey();
                return m1.compareTo(m2);
            }
        });
        Iterator<Map.Entry<Object, Object>> i = al.iterator();
        while (i.hasNext()) {
            Map.Entry<Object, Object> row = i.next();
            tableModel.addRow(new String[]{(String) row.getKey(), (String) row.getValue()});
        }
        deleteButton.setEnabled(true);
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
    }

    private Component makeMainPanel() {
        initializeTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the add and delete buttons.
     *
     * @return a GUI panel containing the buttons
     */
    private JPanel makeButtonPanel() {// Not currently used
        addButton = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addButton.setActionCommand(ADD);
        addButton.setEnabled(true);

        deleteButton = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        deleteButton.setActionCommand(DELETE);

        loadButton = new JButton(JMeterUtils.getResString("load"));
        loadButton.setActionCommand(LOAD);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        loadButton.addActionListener(this);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(loadButton);
        return buttonPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        JPanel p = this;

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        p = new JPanel();

        p.setLayout(new BorderLayout());

        p.add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        p.add(Box.createVerticalStrut(100), BorderLayout.WEST);
        p.add(makeButtonPanel(), BorderLayout.SOUTH);

        add(p, BorderLayout.CENTER);
        table.revalidate();
        setUpData();
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.testInfoNotificationListeners.add(new BmTestManager.TestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                if (testInfo.getJmeterProperties() == null) {
                    testInfo.setJmeterProperties(getData());
                }

            }

        });
    }

    private void initializeTableModel() {
        tableModel = new PowerTableModel(new String[]{NAME, VALUE},
                new Class[]{String.class, String.class});
    }

    public Properties getData() {
        Properties p = new Properties();
        Data data = tableModel.getData();
        String[] names = data.getColumn(NAME);
        String[] values = data.getColumn(VALUE);
        int dataSize = data.size();
        for (int i = 0; i < dataSize; i++) {
            p.put(names[i], values[i]);
        }
        return p;
    }
}