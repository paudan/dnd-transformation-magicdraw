/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ktu.dndtransformations.parsers;

import org.ktu.transformations.parsers.ElementMappingFactory;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.PatternParser;

/**
 *
 * @author Admin
 */
public class MagicDrawMappingFactory extends ElementMappingFactory {

    @Override
    public ElementMapping createElementMapping(PatternParser owner) {
        return new MDElementMapping(owner);
    }
    
}
