package com.softwaremosaic.junit.examples.random_input;

import com.softwaremosaic.junit.JUnitMosaicRunner;
import com.softwaremosaic.junit.annotations.Test;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * When @Test is used on a method that takes parameters, random data will be generated to pass in
 * to the method.  This is great for testing a range of input values, but painful when the test fails.
 * After all, the next run will use different numbers and could succeed or fail in different ways.  To
 * solve this problem, a failure will print out text similar to the following:
 *
 * <pre>
 *     Test failed with: java.lang.AssertionError  bang
 *     To repeat the test with the same values from the random value generators, specify @Test(seed=1415307122233)
 *     The generated values used for this test run were:
 *       0: '-1693200118,704537479,-102607402,-719872670,942851756,1737697596,985873653,-1655980812,-547581154,2047563415,-1721102880,-405120095,2067399498,2021857898,1936855377,199777751,-606684305,193267704,-2010152863,-967448390,2131606809,1322140184,734917182,-779439310,-1097843394,1719498143,1029577571,-896914869,2122369806,-1674140688,1886182507,-1233558816,484231089,537246851,872259311,-150938967,1865296430,2013923801,570558012,523505070,289380473,1093953905,1806232511,-437725661,617401230,581064336,673099571,1705549834,-412530416,-1134573808,-996617235,-173708252,823617193,-375794917,365359055,710713753,55157187,-443797208,-1274877287,-137252738,-850901247,-1045200948,1557826177,-2104851225,-264935616,588144982,231494788,-407385832,1512644341,1802986677,1834072660,674428889,-987747418,1264697563,-527394835,1832665060,1136055357,931323907,1565393806,287454303,1256857034,1697822861,-1594070136'
 *       1: '1431917337,-1081280359,-151059335,300502528,-1849566587,-1097007101,561809358,1214660163,-877699860,689875908,1230717764,1909163413,-493208696,-1868697899,322991689,964073867,-1326557241,-956350936,413991112,-1801480918,968108051,-1548039508,227631565,1677564418,1356210164,777292725,-873265537,530908033,-1220778862,-1428481685,1484580569,-863409761,-784804443,343365540,-1273661788,1045262927,-1292017136,1230434296,-137108949,-1823836835,1008119668,-1971610536,-907108966,-335942135,1613041549,-113687978,-1283636494,-1708474988,-2008168320,1802296284,1076347065,1739341498,773761444,1186345977,438098105,525936232,1843261996,306081508,-2048266625,917256089,1400932527,366307043,-1156701028,1363723728,314605940,-1401957004,-1755132377,1528891261,806220752,-414167654'
 * </pre>
 *
 * To repeat the test using the same generated values, specify the random number seed on the @Test annotation.
 * In the example above, the seed was printed out to stderr and was 1415307122233.
 */
@RunWith(JUnitMosaicRunner.class)
public class RandomSeedTests {

    @Test
    public void noSeedSpecified_expectARandomNumberToBeUsed() {
        int v = PrimitiveGenerators.integers().next();

        assertTrue( v != 958525903 );
    }

    @Test(seed=123)
    public void specifySeed_expectRandomNumberToBeTheSameEachTime() {
        int v = PrimitiveGenerators.integers().next();

        assertEquals( 958525903, v );
    }

    @Test(seed=456)
    public void specifyAnotherSeed_expectRandomNumberToBeTheSameEachTime() {
        int v = PrimitiveGenerators.integers().next();

        assertEquals( 1112040761, v );
    }

}
