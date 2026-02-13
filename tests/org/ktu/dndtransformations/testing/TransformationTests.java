package org.ktu.dndtransformations.testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BPMN_UMLGenerationTest.class, BPMN_UMLSingleElementGenerationTest.class, 
    SOAML_SBVR_BPMNGenerationTest.class, SOAML_SBVR_BPMNSingleElementGenerationTest.class })
public class TransformationTests {

}
