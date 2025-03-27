package jp.co.tac.gui;

import jp.co.tac.model.AwsInstance;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class InstanceTableModel extends AbstractTableModel {
    private final List<AwsInstance> instances;
    private final String[] columnNames = {
            "Name",
            "Instance ID",
            "Environment",
            "Region",
            "Type",
            "OS",
            "Private IP",
            "Public IP"
    };

    public InstanceTableModel(List<AwsInstance> instances) {
        this.instances = instances;
    }

    @Override
    public int getRowCount() {
        return instances.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AwsInstance instance = instances.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return instance.getName();
            case 1:
                return instance.getInstanceId();
            case 2:
                return instance.getEnvironment();
            case 3:
                return instance.getRegion();
            case 4:
                return instance.getInstanceType();
            case 5:
                return instance.getOsType();
            case 6:
                return instance.getPrivateIp();
            case 7:
                return instance.getPublicIp();
            default:
                return null;
        }
    }

    public AwsInstance getInstanceAt(int rowIndex) {
        return instances.get(rowIndex);
    }
}