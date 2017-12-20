package com.dynatrace.openkit.test;

import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.core.DeviceImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeviceImplTest {

    @Test
    public void defaultValueForModelIdIsCorrect() {
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_DEVICE_ID, is(device.getModelID()));
    }

    @Test
    public void settingModelIdUsingValidValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_DEVICE_ID, is(device.getModelID()));

        String validModelId = "testDevice";
        device.setModelID(validModelId);

        assertThat(validModelId, is(device.getModelID()));
    }

    @Test
    public void settingModelIdUsingEmptyValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_DEVICE_ID, is(device.getModelID()));

        String emptyModelId = "";
        device.setModelID(emptyModelId);

        assertThat(OpenKitConstants.DEFAULT_DEVICE_ID, is(device.getModelID()));
    }

    @Test
    public void settingModelIdUsingUndefinedValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_DEVICE_ID, is(device.getModelID()));

        String undefinedModelId = null;
        device.setModelID(undefinedModelId);

        assertThat(OpenKitConstants.DEFAULT_DEVICE_ID, is(device.getModelID()));
    }

    @Test
    public void defaultValueForOperatingSystemIsCorrect() {
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_OPERATING_SYSTEM, is(device.getOperatingSystem()));
    }

    @Test
    public void settingOperatingSystemUsingValidValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_OPERATING_SYSTEM, is(device.getOperatingSystem()));

        String validModelId = "testDevice";
        device.setOperatingSystem(validModelId);

        assertThat(validModelId, is(device.getOperatingSystem()));
    }

    @Test
    public void settingOperatingSystemUsingEmptyValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_OPERATING_SYSTEM, is(device.getOperatingSystem()));

        String emptyOperatingSystem = "";
        device.setOperatingSystem(emptyOperatingSystem);

        assertThat(OpenKitConstants.DEFAULT_OPERATING_SYSTEM, is(device.getOperatingSystem()));
    }

    @Test
    public void settingOperatingSystemUsingUndefinedValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_OPERATING_SYSTEM, is(device.getOperatingSystem()));

        String undefinedOperatingSystem = null;
        device.setOperatingSystem(undefinedOperatingSystem);

        assertThat(OpenKitConstants.DEFAULT_OPERATING_SYSTEM, is(device.getOperatingSystem()));
    }

    @Test
    public void defaultValueForMancufacturerModelIdIsCorrect() {
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_MANUFACTURER, is(device.getManufacturer()));
    }

    @Test
    public void settingManufacturerUsingValidValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_MANUFACTURER, is(device.getManufacturer()));

        String validManufacturer = "testManufacturer";
        device.setManufacturer(validManufacturer);

        assertThat(validManufacturer, is(device.getManufacturer()));
    }

    @Test
    public void settingManufacturerUsingEmptyValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_MANUFACTURER, is(device.getManufacturer()));

        String emptyManufacturer = "";
        device.setManufacturer(emptyManufacturer);

        assertThat(OpenKitConstants.DEFAULT_MANUFACTURER, is(device.getManufacturer()));
    }

    @Test
    public void settingManufacturerUsingUndefinedValue(){
        DeviceImpl device = new DeviceImpl();

        assertThat(OpenKitConstants.DEFAULT_MANUFACTURER, is(device.getManufacturer()));

        String undefinedManufacturer = null;
        device.setManufacturer(undefinedManufacturer);

        assertThat(OpenKitConstants.DEFAULT_MANUFACTURER, is(device.getManufacturer()));
    }
}
