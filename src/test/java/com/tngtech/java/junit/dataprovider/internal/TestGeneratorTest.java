package com.tngtech.java.junit.dataprovider.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.tngtech.java.junit.dataprovider.BaseTest;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter.Settings;

@RunWith(MockitoJUnitRunner.class)
public class TestGeneratorTest extends BaseTest {

    @InjectMocks
    private TestGenerator underTest;

    @Mock
    private DataConverter dataConverter;
    @Mock
    private FrameworkMethod testMethod;
    @Mock
    private FrameworkMethod dataProviderMethod;
    @Mock
    private DataProvider dataProvider;

    @Before
    public void setup() {
        doReturn(anyMethod()).when(testMethod).getMethod();
        doReturn(anyMethod()).when(dataProviderMethod).getMethod();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("DLS_DEAD_LOCAL_STORE")
    @Test(expected = NullPointerException.class)
    public void testTestGeneratorShouldThrowNullPointerExceptionIfDataConverterIsNull() {
        // Given:

        // When:
        @SuppressWarnings("unused")
        TestGenerator result = new TestGenerator(null);

        // Then: expect exception
    }

    @Test
    public void testGenerateExplodedTestMethodsForShouldReturnEmptyListIfArgumentIsNull() {
        // Given:

        // When:
        List<FrameworkMethod> result = underTest.generateExplodedTestMethodsFor(null, null);

        // Then:
        assertThat(result).isEmpty();
    }

    @Test
    public void testGenerateExplodedTestMethodsForShouldReturnOriginalTestMethodIfNoDataProviderIsUsed() {
        // Given:

        // When:
        List<FrameworkMethod> result = underTest.generateExplodedTestMethodsFor(testMethod, null);

        // Then:
        assertThat(result).containsOnly(testMethod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExplodeTestMethodsUseDataProviderShouldThrowIllegalArgumentExceptionIfDataProviderMethodThrowsException()
            throws Throwable {
        // Given:
        doThrow(NullPointerException.class).when(dataProviderMethod).invokeExplosively(null);

        // When:
        underTest.explodeTestMethod(testMethod, dataProviderMethod);

        // Then: expect exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExplodeTestMethodsUseDataProviderShouldThrowIllegalArgumentExceptionIfDataConverterReturnsEmpty() {
        // Given:
        doReturn(new ArrayList<Object[]>()).when(dataConverter).convert(any(), any(Class[].class), any(Settings.class));
        doReturn(dataProvider).when(dataProviderMethod).getAnnotation(DataProvider.class);

        // When:
        underTest.explodeTestMethod(testMethod, dataProviderMethod);

        // Then: expect exception
    }

    @Test
    public void testExplodeTestMethodsUseDataProviderShouldReturnOneDataProviderFrameworkMethodIfDataConverterReturnsOneRow() {
        // Given:
        List<Object[]> dataConverterResult = listOfArrays(new Object[] { 1, 2, 3 });
        doReturn(dataConverterResult).when(dataConverter).convert(any(), any(Class[].class), any(Settings.class));
        doReturn(dataProvider).when(dataProviderMethod).getAnnotation(DataProvider.class);

        // When:
        List<FrameworkMethod> result = underTest.explodeTestMethod(testMethod, dataProviderMethod);

        // Then:
        assertDataProviderFrameworkMethods(result, dataConverterResult);
        verify(dataConverter).checkIfArgumentsMatchParameterTypes(eq(dataConverterResult), any(Class[].class));
    }

    @Test
    public void testExplodeTestMethodsUseDataProviderShouldReturnMultipleDataProviderFrameworkMethodIfDataConverterReturnsMultipleRows() {
        // Given:
        List<Object[]> dataConverterResult = listOfArrays(new Object[] { 11, "22", 33L },
                new Object[] { 44, "55", 66L }, new Object[] { 77, "88", 99L });
        doReturn(dataConverterResult).when(dataConverter).convert(any(), any(Class[].class), any(Settings.class));
        doReturn(dataProvider).when(dataProviderMethod).getAnnotation(DataProvider.class);

        // When:
        List<FrameworkMethod> result = underTest.explodeTestMethod(testMethod, dataProviderMethod);

        // Then:
        assertDataProviderFrameworkMethods(result, dataConverterResult);
        verify(dataConverter).checkIfArgumentsMatchParameterTypes(eq(dataConverterResult), any(Class[].class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExplodeTestMethodsDataProviderShouldIllegalArgumentExceptionIfDataConverterReturnsAnEmptyList() {
        // Given:
        doReturn(new ArrayList<Object[]>()).when(dataConverter).convert(any(), any(Class[].class), any(Settings.class));

        // When:
        underTest.explodeTestMethod(testMethod, dataProvider);

        // Then: expect exception
    }

    @Test
    public void testExplodeTestMethodsDataProviderShouldReturnOneDataProviderFrameworkMethodIfDataConverterReturnsOneRow() {
        // Given:
        List<Object[]> dataConverterResult = listOfArrays(new Object[] { 1, "test1" });
        doReturn(dataConverterResult).when(dataConverter).convert(any(), any(Class[].class), any(Settings.class));

        // When:
        List<FrameworkMethod> result = underTest.explodeTestMethod(testMethod, dataProvider);

        // Then:
        assertDataProviderFrameworkMethods(result, dataConverterResult);
        verify(dataConverter).checkIfArgumentsMatchParameterTypes(eq(dataConverterResult), any(Class[].class));
    }

    @Test
    public void testExplodeTestMethodsDataProviderShouldReturnMultipleDataProviderFrameworkMethodIfDataProviderValueArrayReturnsMultipleRows() {
        // Given:
        List<Object[]> dataConverterResult = listOfArrays(new Object[] { "2a", "foo" }, new Object[] { "3b", "bar" },
                new Object[] { "4c", "baz" });
        doReturn(dataConverterResult).when(dataConverter).convert(any(), any(Class[].class), any(Settings.class));

        // When:
        List<FrameworkMethod> result = underTest.explodeTestMethod(testMethod, dataProvider);

        // Then:
        assertDataProviderFrameworkMethods(result, dataConverterResult);
        verify(dataConverter).checkIfArgumentsMatchParameterTypes(eq(dataConverterResult), any(Class[].class));
    }
}
