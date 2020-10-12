/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author florian
 */
public class RandomTools
{
    public static String getRandomString(int length, String chars)
    {
        List<Character> temp = chars.chars()
                .mapToObj(i -> (char)i)
                .collect(Collectors.toList());
        Collections.shuffle(temp, new SecureRandom());
        return  temp.stream()
                .map(Object::toString)
                .limit(length)
                .collect(Collectors.joining());
    }
}
