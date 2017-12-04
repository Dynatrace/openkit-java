package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.DeviceImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeviceImplTest {

    @Test
    public void defaultValueForModelIdIsCorrect() {
        DeviceImpl device = new DeviceImpl();
        String expectedModelId = DeviceImpl.DEFAULT_DEVICE_ID;

        assertThat(expectedModelId, is(device.getModelID()));
    }

    @Test
    public void settingModelIdUsingValidValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedModelId = DeviceImpl.DEFAULT_DEVICE_ID;

        assertThat(expectedModelId, is(device.getModelID()));

        String validModelId = "testDevice";
        device.setModelID(validModelId);

        assertThat(validModelId, is(device.getModelID()));
    }

    @Test
    public void settingModelIdUsingEmptyValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedModelId = DeviceImpl.DEFAULT_DEVICE_ID;

        assertThat(expectedModelId, is(device.getModelID()));

        String emptyModelId = "";
        device.setModelID(emptyModelId);

        assertThat(expectedModelId, is(device.getModelID()));
    }

    @Test
    public void settingModelIdUsingUndefinedValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedModelId = DeviceImpl.DEFAULT_DEVICE_ID;

        assertThat(expectedModelId, is(device.getModelID()));

        String undefinedModelId = null;
        device.setModelID(undefinedModelId);

        assertThat(expectedModelId, is(device.getModelID()));
    }

    @Test
    public void defaultValueForOperatingSystemIsCorrect() {
        DeviceImpl device = new DeviceImpl();
        String expectedOperatingSystem = DeviceImpl.DEFAULT_OPERATING_SYSTEM;

        assertThat(expectedOperatingSystem, is(device.getOperatingSystem()));
    }

    @Test
    public void settingOperatingSystemUsingValidValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedOperatingSystem = DeviceImpl.DEFAULT_OPERATING_SYSTEM;

        assertThat(expectedOperatingSystem, is(device.getOperatingSystem()));

        String validModelId = "testDevice";
        device.setOperatingSystem(validModelId);

        assertThat(validModelId, is(device.getOperatingSystem()));
    }

    @Test
    public void settingOperatingSystemUsingEmptyValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedOperatingSystem = DeviceImpl.DEFAULT_OPERATING_SYSTEM;

        assertThat(expectedOperatingSystem, is(device.getOperatingSystem()));

        String emptyOperatingSystem = "";
        device.setOperatingSystem(emptyOperatingSystem);

        assertThat(expectedOperatingSystem, is(device.getOperatingSystem()));
    }

    @Test
    public void settingOperatingSystemUsingUndefinedValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedOperatingSystem = DeviceImpl.DEFAULT_OPERATING_SYSTEM;

        assertThat(expectedOperatingSystem, is(device.getOperatingSystem()));

        String undefinedOperatingSystem = null;
        device.setOperatingSystem(undefinedOperatingSystem);

        assertThat(expectedOperatingSystem, is(device.getOperatingSystem()));
    }

    @Test
    public void defaultValueForMancufacturerModelIdIsCorrect() {
        DeviceImpl device = new DeviceImpl();
        String expectedManufacturer = DeviceImpl.DEFAULT_MANUFACTURER;

        assertThat(expectedManufacturer, is(device.getManufacturer()));
    }

    @Test
    public void settingManufacturerUsingValidValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedManufacturer = DeviceImpl.DEFAULT_MANUFACTURER;

        assertThat(expectedManufacturer, is(device.getManufacturer()));

        String validManufacturer = "testManufacturer";
        device.setManufacturer(validManufacturer);

        assertThat(validManufacturer, is(device.getManufacturer()));
    }

    @Test
    public void settingManufacturerUsingEmptyValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedManufacturer = DeviceImpl.DEFAULT_MANUFACTURER;

        assertThat(expectedManufacturer, is(device.getManufacturer()));

        String emptyManufacturer = "";
        device.setManufacturer(emptyManufacturer);

        assertThat(expectedManufacturer, is(device.getManufacturer()));
    }

    @Test
    public void settingManufacturerUsingUndefinedValue(){
        DeviceImpl device = new DeviceImpl();
        String expectedManufacturer = DeviceImpl.DEFAULT_MANUFACTURER;

        assertThat(expectedManufacturer, is(device.getManufacturer()));

        String undefinedManufacturer = null;
        device.setManufacturer(undefinedManufacturer);

        assertThat(expectedManufacturer, is(device.getManufacturer()));
    }
}
