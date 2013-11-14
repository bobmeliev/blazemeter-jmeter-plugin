package com.blazemeter.jmeter.testexecutor;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

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

    private static final String COLUMN_NAMES_0 = "name"; // $NON-NLS-1$

    private static final String COLUMN_NAMES_1 = "value"; // $NON-NLS-1$

    // TODO: add and delete not currently supported
    private static final String ADD = "add"; // $NON-NLS-1$

    private static final String DELETE = "delete"; // $NON-NLS-1$

    private static final String SYSTEM = "system"; // $NON-NLS-1$

    private static final String JMETER = "jmeter"; // $NON-NLS-1$

//    private final JCheckBox systemButton = new JCheckBox("System");

    private final JCheckBox jmeterButton = new JCheckBox("JMeter");


    /**
     * The table containing the list of arguments.
     */
    private transient JTable table;

    /**
     * The model for the arguments table.
     */
//    protected transient ObjectTableModel tableModel;
    protected transient PowerTableModel tableModel;

    /**
     * A button for adding new arguments to the table.
     */
    private JButton addButton;

    /**
     * A button for removing arguments from the table.
     */
    private JButton deleteButton;

    public JMeterPropertyPanel() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "property_visualiser_title"; // $NON-NLS-1$
    }

    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(new String[]{MenuFactory.NON_TEST_ELEMENTS});
    }

    @Override
    public void actionPerformed(ActionEvent action) {
        String command = action.getActionCommand();
        if (ADD.equals(command)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(cookieTable);
            GuiUtils
            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }


            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            cookieTable.setRowSelectionInterval(rowToSelect, rowToSelect);

//            return;
        }
        if (DELETE.equals(command)) {
            if (tableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                if (cookieTable.isEditing()) {
                    TableCellEditor cellEditor = cookieTable.getCellEditor(cookieTable.getEditingRow(),
                            cookieTable.getEditingColumn());
                    cellEditor.cancelCellEditing();
                }

                int rowSelected = cookieTable.getSelectedRow();

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

                        cookieTable.setRowSelectionInterval(rowToSelect, rowToSelect);
                    }
                }
            }

//            return;
        }
        if (SYSTEM.equals(command)) {
            setUpData();
            return;
        }
        if (JMETER.equals(command)) {
            setUpData();
            return;
        }

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
        if (systemButton.isSelected()) {
            p = System.getProperties();
        }
        if (jmeterButton.isSelected()) {
            p = JMeterUtils.getJMeterProperties();
        }
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
            tableModel.addRow(i.next());
        }

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
        add = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        add.setActionCommand(ADD);
        add.setEnabled(true);

        delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        delete.setActionCommand(DELETE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add.addActionListener(this);
        delete.addActionListener(this);
        buttonPanel.add(add);
        buttonPanel.add(delete);
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

    }

    private void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[]{COLUMN_NAMES_0, COLUMN_NAMES_1},
                new Functor[]{
                        new Functor(Map.Entry.class, "getKey"), // $NON-NLS-1$
                        new Functor(Map.Entry.class, "getValue")},  // $NON-NLS-1$
                new Functor[]{
                        null, //new Functor("setName"), // $NON-NLS-1$
                        new Functor(Map.Entry.class, "setValue", new Class[]{Object.class}) // $NON-NLS-1$
                },
                new Class[]{String.class, String.class});
    }
}