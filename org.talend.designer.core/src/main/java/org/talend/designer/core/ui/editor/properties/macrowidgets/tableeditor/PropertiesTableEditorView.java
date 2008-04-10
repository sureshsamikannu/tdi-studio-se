// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.LAYOUT_MODE;
import org.talend.commons.ui.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.swt.tableviewer.behavior.IColumnColorProvider;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * MetadataTableEditorView2 must be used.
 * 
 * $Id: MetadataTableEditorView.java 801 2006-11-30 16:28:36Z amaumont $
 * 
 * @param <B>
 */
public class PropertiesTableEditorView<B> extends AbstractDataTableEditorView<B> {

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     * 
     * @param parent
     * @param style
     * @param model
     */
    public PropertiesTableEditorView(Composite parent, int style, PropertiesTableEditorModel model) {
        super(parent, style, model);
    }

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param readOnly
     * @param toolbarVisible
     * @param labelVisible TODO
     * @param extendedTableModel
     */
    public PropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle, PropertiesTableEditorModel model,
            boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, model, model.getElemParameter().isReadOnly(), toolbarVisible, labelVisible);
    }

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     */
    public PropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    public void setMetadataTableEditor(PropertiesTableEditorModel model) {
        setExtendedTableModel(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#initToolBar()
     */
    @Override
    protected ExtendedToolbarView initToolBar() {
        return new PropertiesTableToolbarEditorView(getMainComposite(), SWT.NONE, this.getExtendedTableViewer());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#setTableViewerCreatorOptions(org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
     */
    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<B> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
        newTableViewerCreator.setLayoutMode(LAYOUT_MODE.DEFAULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#createColumns(org.talend.commons.ui.swt.tableviewer.TableViewerCreator,
     * org.eclipse.swt.widgets.Table)
     */
    @Override
    protected void createColumns(final TableViewerCreator<B> tableViewerCreator, final Table table) {

        // there's two lists of values, one that will be in the table
        // and the other will be stored as the current value in the property
        // there is two lists because of the undo / redo capabilities
        PropertiesTableEditorModel model = getModel();
        TalendProposalProvider processProposalProvider = new TalendProposalProvider(model.getProcess());
        String[] titles = model.getTitles();
        final Object[] itemsValue = model.getItemsValue();
        final String[] items = model.getItems();
        // final Element elem = model.getElement();
        final IElementParameter param = model.getElemParameter();
        final IElement element = model.getElement();
        for (int i = 0; i < titles.length; i++) {
            final int curCol = i;
            if (param.isShow(model.getItemsShowIf()[i], model.getItemsNotShowIf()[i], element.getElementParameters())) {
                TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
                column.setTitle(titles[i]);
                column.setModifiable(true);
                column.setMinimumWidth(100);
                column.setWeight(20);

                IElementParameter tmpParam = (IElementParameter) itemsValue[i];
                switch (tmpParam.getField()) {
                case CONTEXT_PARAM_NAME_LIST:
                case CLOSED_LIST:
                case LOOKUP_COLUMN_LIST:
                case COLUMN_LIST:
                case CONNECTION_LIST:
                case DBTYPE_LIST:
                case COMPONENT_LIST:
                case PREV_COLUMN_LIST:
                    final ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(table, tmpParam.getListItemsDisplayName());
                    final IElementParameter copyOfTmpParam = tmpParam;
                    ((CCombo) cellEditor.getControl()).setEditable(false);
                    ((CCombo) cellEditor.getControl())
                            .setEnabled(!(param.isRepositoryValueUsed() || param.isReadOnly() || tmpParam.isReadOnly()));
                    column.setCellEditor(cellEditor, new CellEditorValueAdapter() {

                        @Override
                        public String getColumnText(CellEditor cellEditor, Object bean, Object cellEditorValue) {
                            return (String) cellEditorValue;
                        }

                        @Override
                        public Object getOriginalTypedValue(CellEditor cellEditor, Object cellEditorTypedValue) {
                            Object returnedValue = null;
                            if (cellEditorTypedValue != null && cellEditorTypedValue instanceof Integer) {
                                int index = (Integer) cellEditorTypedValue;
                                String[] namesSet = ((CCombo) cellEditor.getControl()).getItems();
                                if (namesSet.length > 0 && index > -1 && index < namesSet.length) {
                                    returnedValue = namesSet[index];
                                } else {
                                    returnedValue = null;
                                }
                            } else {
                                returnedValue = null;
                            }
                            return returnedValue;
                        };

                        @Override
                        public Object getCellEditorTypedValue(CellEditor cellEditor, Object originalTypedValue) {
                            CCombo combo = (CCombo) cellEditor.getControl();
                            int rowNumber = ((Table) combo.getParent()).getSelectionIndex();
                            String[] listToDisplay = getItemsToDisplay(element, copyOfTmpParam, rowNumber);
                            if (!Arrays.equals(listToDisplay, ((ComboBoxCellEditor) cellEditor).getItems())) {
                                ((ComboBoxCellEditor) cellEditor).setItems(listToDisplay);
                            }
                            Object returnedValue = 0;
                            if (originalTypedValue != null) {
                                String[] namesSet = listToDisplay;
                                for (int j = 0; j < namesSet.length; j++) {
                                    if (namesSet[j].equals(originalTypedValue)) {
                                        returnedValue = j;
                                        break;
                                    }
                                }
                            }
                            return returnedValue;
                        };
                    });
                    break;
                case COLOR:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly()) && (!tmpParam.isReadOnly()));
                    // column.setDisplayedValue("");

                    column.setLabelProvider(null);
                    column.setCellEditor(new ColorCellEditor(table) {

                        @Override
                        protected void doSetValue(Object value) {
                            if (value instanceof String) {
                                super.doSetValue(TalendTextUtils.stringToRGB((String) value));
                            } else {
                                super.doSetValue(value);
                            }
                        }

                        @Override
                        protected void updateContents(Object value) {
                            if (value != null) {
                                if (value instanceof String) {
                                    super.updateContents(TalendTextUtils.stringToRGB((String) value));
                                } else {
                                    super.updateContents(value);
                                }
                            }
                        }

                    });
                    column.setColorProvider(new IColumnColorProvider() {

                        public Color getBackgroundColor(Object bean) {
                            Object value = ((Map<String, Object>) bean).get(items[curCol]);
                            if (value == null || (!(value instanceof String))) {
                                return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE); //$NON-NLS-1$
                            }
                            return new Color(null, TalendTextUtils.stringToRGB((String) value));
                        }

                        public Color getForegroundColor(Object bean) {
                            return null;
                        }

                    });
                    break;
                case CHECK:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly()) && (!tmpParam.isReadOnly()));
                    column.setTableEditorContent(new CheckboxTableEditorContent());
                    column.setDisplayedValue(""); //$NON-NLS-1$
                    break;
                default: // TEXT
                    TextCellEditorWithProposal textCellEditor = new TextCellEditorWithProposal(table, column);
                    textCellEditor.setContentProposalProvider(processProposalProvider);
                    if (((i == 0) && (param.isBasedOnSchema())) || (param.isRepositoryValueUsed()) || (param.isReadOnly())
                            || tmpParam.isReadOnly()) {
                        // read only cell
                    } else {
                        // writable cell
                        column.setCellEditor(textCellEditor);
                    }

                }
                column.setBeanPropertyAccessors(new IBeanPropertyAccessors<B, Object>() {

                    public Object get(B bean) {
                        Object value = ((Map<String, Object>) bean).get(items[curCol]);
                        if (value == null) {
                            return ""; //$NON-NLS-1$
                        }
                        if (itemsValue[curCol] instanceof IElementParameter) {
                            IElementParameter tmpParam = (IElementParameter) itemsValue[curCol];
                            switch (tmpParam.getField()) {
                            case CONTEXT_PARAM_NAME_LIST:
                            case CLOSED_LIST:
                            case COMPONENT_LIST:
                            case COLUMN_LIST:
                            case CONNECTION_LIST:
                            case LOOKUP_COLUMN_LIST:
                            case PREV_COLUMN_LIST:
                            case DBTYPE_LIST:
                                String[] namesSet = tmpParam.getListItemsDisplayName();
                                if (namesSet.length == 0) {
                                    return tmpParam.getDefaultClosedListValue();
                                }
                                if (value instanceof String) {
                                    boolean found = false;
                                    int index = 0;
                                    Object[] items = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                    for (int j = 0; j < items.length && !found; j++) {
                                        if (items[j].equals(value)) {
                                            found = true;
                                            index = j;
                                        }
                                    }
                                    value = new Integer(index);
                                }
                                if (value != null && ((Integer) value) >= 0) {
                                    return namesSet[(Integer) value];
                                }
                                return null;
                            case CHECK:
                                if (value instanceof String) {
                                    return new Boolean((String) value);
                                }
                                return value;
                            case RADIO:
                                if (value instanceof String) {
                                    return new Boolean((String) value);
                                }
                                return value;
                            case COLOR:
                                if (value instanceof String) {
                                    return TalendTextUtils.stringToRGB((String) value);
                                }
                                return value; // already RGB
                            default: // TEXT
                                return value;
                            }
                        }
                        return value;
                    }

                    public void set(B bean, Object value) {
                        Object finalValue = value;
                        IElementParameter tmpParam = (IElementParameter) itemsValue[curCol];
                        switch (tmpParam.getField()) {
                        case CONTEXT_PARAM_NAME_LIST:
                        case CLOSED_LIST:
                        case COLUMN_LIST:
                        case COMPONENT_LIST:
                        case CONNECTION_LIST:
                        case LOOKUP_COLUMN_LIST:
                        case PREV_COLUMN_LIST:
                            if (value instanceof String) {
                                Object[] itemNames = ((IElementParameter) itemsValue[curCol]).getListItemsDisplayName();
                                Object[] itemValues = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                boolean found = false;
                                int index = 0;
                                for (int j = 0; j < itemNames.length && !found; j++) {
                                    if (itemNames[j].equals(value)) {
                                        found = true;
                                        index = j;
                                    }
                                }
                                if (value != null && (index >= 0)) {
                                    finalValue = itemValues[new Integer(index)];
                                }
                            }
                            break;
                        case COLOR:
                            if (value instanceof RGB) {
                                RGB rgb = (RGB) value;
                                finalValue = rgb.red + ";" + rgb.green + ";" + rgb.blue;
                            }
                        default:
                        }
                        ((Map<String, Object>) bean).put(items[curCol], finalValue);
                        resetValuesIfNeeded(element, param, (Map<String, Object>) bean);
                    }
                });
            }
        }
    }

    public PropertiesTableToolbarEditorView getToolBar() {
        return (PropertiesTableToolbarEditorView) getExtendedToolbar();
    }

    public PropertiesTableEditorModel getModel() {
        return (PropertiesTableEditorModel) getExtendedTableModel();
    }

    private void resetValuesIfNeeded(IElement element, IElementParameter mainParam, Map<String, Object> currentLine) {
        List<Map<String, Object>> tableValues = (List<Map<String, Object>>) mainParam.getValue();
        int rowNumber = tableValues.indexOf(currentLine);
        if (rowNumber != -1) {
            for (Object item : mainParam.getListItemsValue()) {
                if (item instanceof IElementParameter) {
                    IElementParameter curParam = (IElementParameter) item;
                    switch (curParam.getField()) {
                    case CLOSED_LIST:
                        String[] itemsToDisplay = getItemsToDisplay(element, curParam, rowNumber);
                        Object currentValue = currentLine.get(curParam.getName());
                        String currentDisplay = null;
                        if (currentValue instanceof Integer) {
                            currentDisplay = curParam.getListItemsDisplayName()[(Integer) currentValue];
                        } else if (currentValue instanceof String) {
                            int index = ArrayUtils.indexOf(curParam.getListItemsValue(), currentValue);
                            if (index != -1) {
                                currentDisplay = curParam.getListItemsDisplayName()[index];
                            }
                        }

                        if (currentDisplay != null) {
                            // if the current displayed shouldn't be used anymore, reset the value.
                            if (!ArrayUtils.contains(itemsToDisplay, currentDisplay)) {
                                String newValue = "";
                                if (itemsToDisplay.length > 0) {
                                    int index = ArrayUtils.indexOf(curParam.getListItemsDisplayName(), itemsToDisplay[0]);
                                    newValue = (String) curParam.getListItemsValue()[index];
                                }
                                currentLine.put(curParam.getName(), newValue);
                            }
                        }
                        break;
                    default:
                    }

                }
            }
        }
    }

    /**
     * DOC nrousseau Comment method "getItemsToDisplay".
     * 
     * @param element
     * @param param
     * @param rowNumber
     * @return
     */
    private String[] getItemsToDisplay(final IElement element, final IElementParameter param, int rowNumber) {
        if (param instanceof ElementParameter) {
            ((ElementParameter) param).setCurrentRow(rowNumber);
        }
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null && (itemsShowIf.length > 0)) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], element.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (int i = 0; i < originalList.length; i++) {
                stringToDisplay.add(originalList[i]);
            }
        }
        String[] listToDisplay = stringToDisplay.toArray(new String[0]);
        return listToDisplay;
    }
}
