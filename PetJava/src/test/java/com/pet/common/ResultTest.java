package com.pet.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 响应类测试
 */
public class ResultTest {

    @Test
    public void testSuccessWithData() {
        String data = "test data";
        Result<String> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    public void testSuccessWithoutData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testErrorWithCodeAndMessage() {
        Result<Void> result = Result.error(404, "Not Found");

        assertEquals(404, result.getCode());
        assertEquals("Not Found", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testErrorWithMessageOnly() {
        Result<Void> result = Result.error("Something went wrong");

        assertEquals(500, result.getCode());
        assertEquals("Something went wrong", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testResultWithObjectData() {
        class TestData {
            String name = "test";
            int value = 123;
        }

        TestData data = new TestData();
        Result<TestData> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test", result.getData().name);
        assertEquals(123, result.getData().value);
    }

    @Test
    public void testResultChaining() {
        Result<String> result = Result.success("initial");
        
        result.setCode(201);
        result.setMessage("created");
        result.setData("updated");

        assertEquals(201, result.getCode());
        assertEquals("created", result.getMessage());
        assertEquals("updated", result.getData());
    }
}
