package edu.stanford.integrator.service;

import edu.stanford.integrator.services.PreprocessorServiceImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class PreprocessorServiceImplTest {

    private PreprocessorServiceImpl preprocessorService;
    /* exact match */
    @Test
    void stringMatch() {
        assertEquals("asdf","asdf");
    }

    @Test
    void stringNoMatch() {
        assertNotEquals("asdf","qwer");
    }
}
