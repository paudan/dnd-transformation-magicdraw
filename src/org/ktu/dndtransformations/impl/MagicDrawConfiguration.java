package org.ktu.dndtransformations.impl;

import org.ktu.transformations.parsers.DefaultPatternConfiguration;
import org.ktu.transformations.parsers.DefaultSpecificationConfiguration;
import org.ktu.transformations.parsers.PatternConfiguration;
import org.ktu.transformations.parsers.SpecificationConfiguration;

/**
 *
 * @author Admin
 */
public class MagicDrawConfiguration {
    
    private static PatternConfiguration PATTERN_CONFIG;
    private static SpecificationConfiguration SPEC_CONFIG;
    
    public static PatternConfiguration getPatternConfiguration() {
        if (PATTERN_CONFIG == null) 
            PATTERN_CONFIG = new DefaultPatternConfiguration();
        return PATTERN_CONFIG;
    }

    public static SpecificationConfiguration getSpecificationConfiguration() {
        if (SPEC_CONFIG == null) 
            SPEC_CONFIG = new DefaultSpecificationConfiguration();
        return SPEC_CONFIG;
    }
    
}
