/*
 * (c) Abe Laboratory Information Technology Co., Ltd. 2014
 */
package designing.fpij;

import designing.fpij.CalculateNAV;
import java.math.BigDecimal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hiroaki Abe <hiroaki.abe.2004@alit.jp>
 */
public class CalculateNAVTest {
    
    public CalculateNAVTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of computeStockWorth method, of class CalculateNAV.
     */
    @Test
    public void testComputeStockWorth() {
        System.out.println("computeStockWorth");
        
        CalculateNAV instance = new CalculateNAV(ticker -> new BigDecimal("6.01"));
        BigDecimal expected = new BigDecimal("6010.00");
        BigDecimal actual = instance.computeStockWorth("GOOG", 1000);
        assertEquals(expected, actual);
    }
    
}
