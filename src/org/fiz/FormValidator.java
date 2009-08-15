/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz;

import java.util.regex.Pattern;

/**
 * <h1>Usage</h1>
 * The FormValidator class is a collection of validation functions provided
 * by Fiz. Each validation function takes in a set of configuration properties
 * (if applicable) and a dataset containing form data to validate. Validators
 * support the following properties (with additional ones specified for each
 * validator below):
 * 
 *   otherFields:               (optional) List of other form elements to
 *                              include in the form data passed to the
 *                              validator
 *   errorMessage:              (optional) Template for the error message to be
 *                              returned if validation fails.
 *
 * Instructions on attaching form validators to form elements can be seen
 * in {@link FormElement}.
 *
 * <h1>Implementation</h1>
 * Developers may create custom validators by writing their own methods.
 * These methods should take in three arguments: id identifying the form element
 * being validated, properties containing configuration values for the
 * validator, and formData containing all relevant submitted form data needed
 * for the validator. A template for a validation method is:
 *
 * <pre>
 * {@code
 * public static String validateTrue(String id, Dataset properties,
 *         Dataset formData) {
 *     if (formData.get(id).equals("true")) {
 *        return null;
 *     } else {
 *        return FormValidator.errorMessage("Value must be true", properties,
 *                formData);
 *     }
 * }
 * }
 * </pre>
 * 
 * Validator methods may be placed anywhere. They can be referenced in the 
 * {@code type} property of the validator dataset by their fully qualified name,
 * such as "MyValidator.validateTrue".
 */
public class FormValidator {

    /**
     * Generates an error message when validation fails.
     * @param defaultMessage            Default template used to generate error
     *                                  message
     * @param properties                Configuration properties for the
     *                                  validator
     * @param formData                  Data from all relevant form elements
     * @return                          Returns an error message generated by
     *                                  expanding a template with
     *                                  {@code properties} and {@code formData}.
     *                                  If {@code properties} contains an
     *                                  {@code errorMessage} value, then it is
     *                                  used as the template; otherwise,
     *                                  {@code defaultMessage} is used as the
     *                                  template.
     */
    public static String errorMessage(String defaultMessage,
            Dataset properties, Dataset formData) {
        String errorTemplate = properties.check("errorMessage");
        if (errorTemplate != null) {
            defaultMessage = errorTemplate;
        }
            
        return Template.expandRaw(defaultMessage,
                new CompoundDataset(properties, formData));
    }
    
    /**
     * Validates that the value of {@code id} matches the value of all other
     * form elements in {@code otherFields}. The following configuration
     * properties are supported:
     * 
     *   otherFields:            Comma separated list of names of form elements
     *                           that must match the primary form element
     *                           identified by {@code id}
     *   
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateDuplicate(String id, Dataset properties,
            Dataset formData) {
        String[] fields = StringUtil.split(properties.get("otherFields"), ',');
        String inputValue = formData.check(id);
        if (inputValue == null) {
            inputValue = "";
        }
        for (String fieldId : fields) {
            if (!inputValue.equals(formData.check(fieldId))) {
                return errorMessage("@id does not match @" + fieldId,
                        properties, formData);
            }
        }
        return null;
    }

    /**
     * Validates that the value of {@code id} is contained in the list of valid
     * inputs. The following properties are supported:
     * 
     *   valid                      Comma separated list of valid inputs
     *   
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateIn(String id, Dataset properties,
            Dataset formData) {        
        String[] validValues = StringUtil.split(properties.get("valid"), ',');
        for (String value : validValues) {
            if (formData.get(id).equals(value)) {
                return null;
            }
        }

        StringBuilder error = new StringBuilder("Field must match " +
                "one of the following: ");
        for (int i = 0; i < 5 && i < validValues.length; i++) {
            if (i > 0) {
                error.append(", ");
            }
            error.append(validValues[i]);
        }
        if (validValues.length > 5) {
            error.append(", etc.");
        }
        return errorMessage(error.toString(), properties, formData);
    }

    /**
     * Validates that the value of {@code id} is an integer.
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateInteger(String id, Dataset properties,
            Dataset formData) {
        try {
            Integer.parseInt(formData.get(id));
            return null;
        } catch (NumberFormatException e) {
            return errorMessage("Must be an integer", properties, formData);
        }
    }
    
    /**
     * Validates that the length of the value of {@code id} falls within a
     * certain range. The following configuration properties are supported:
     * 
     *   min:                        (optional) Minimum length of input
     *                               Defaults to 0.
     *   max:                        (optional) Maximum length of input,
     *                               Defaults to Integer.MAX_VALUE.
     *   
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateLength(String id, Dataset properties,
            Dataset formData) {
        int length = formData.get(id).length();
        
        String minString = properties.check("min");
        int min = (minString == null ? 0 : Integer.parseInt(minString));

        String maxString = properties.check("max");
        int max = (maxString == null ?
                Integer.MAX_VALUE : Integer.parseInt(maxString));

        if (minString == null && length > max) {
            return errorMessage("Must be at most @max characters long",
                    properties, formData);
        } else if (maxString == null && length < min) {
            return errorMessage("Must be at least @min characters long",
                    properties, formData);
        } else {
            if (length < min || length > max) {
                if(min == max) {
                    return errorMessage("Must be exactly @min characters long",
                            properties, formData);
                } else {
                    return errorMessage("Must be between @min and @max " +
                            "characters long", properties, formData);
                }
            }
        }

        return null;
    }

    /**
     * Validates that the value of {@code id} is a floating-point number.
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateNumeric(String id, Dataset properties,
            Dataset formData) {
        try {
            Double.parseDouble(formData.get(id));
            return null;
        } catch (NumberFormatException e) {
            return errorMessage("Must be a number", properties, formData);
        }
    }
    
    /**
     * Validates that the value of {@code id} is a number that falls between
     * {@code min} and {@code max}. The following properties are supported:
     * 
     *   min:                        (optional) Minimum float value of input
     *   max:                        (optional) Maximum float value of input
     *   includeMin:                 (optional) Whether the minimum value is
     *                               included in the range. Defaults to true.
     *   includeMax:                 (optional) Whether the maximum value is
     *                               included in the range. Defaults to true.
     *                              
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateRange(String id, Dataset properties,
            Dataset formData) {
        double value;
        try {
            value = Double.parseDouble(formData.get(id));
        } catch (NumberFormatException e) {
            return errorMessage("Must be a number", properties, formData);
        }

        boolean validationFailed = false;
        
        // Setup the parameters for the lower bound on the range check
        boolean includeMin = !("false".equals(properties.check("includeMin")));
        String minString = properties.check("min");
        if (minString != null) {
            double min = Double.parseDouble(minString);
            if (value < min || (!includeMin && (value == min))) {
                validationFailed = true;
            }
        }

        // Setup the parameters for the upper bound on the range check
        boolean includeMax = !("false".equals(properties.check("includeMax")));
        String maxString = properties.check("max");
        if (maxString != null) {
            double max = Double.parseDouble(maxString);
            if (value > max || (!includeMax && (value == max))) {
                validationFailed = true;
            }
        }
        
        if (!validationFailed) {
            return null;
        } else {
            // Generate the error message that will be returned if
            // validation fails
            StringBuilder errorMessage = new StringBuilder("Must be ");
            if (minString != null) {
                errorMessage.append(includeMin ? ">= @min" : "> @min");
            }
            if (minString != null && maxString != null) {
                errorMessage.append(" and ");
            }
            if (maxString != null) {
                errorMessage.append(includeMax ? "<= @max" : "< @max");
            }
            return errorMessage(errorMessage.toString(), properties, formData);
        }
    }

    /**
     * Validates that the value of {@code id} matches a regular expression
     * pattern. The following properties are supported:
     * 
     *   pattern:                    Regular expression pattern for validating
     *                               the format of the formData
     *   
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateRegexp(String id, Dataset properties,
            Dataset formData) {
        if (Pattern.matches(properties.get("pattern"), formData.get(id))) {
            return null;
        } else {
            return errorMessage("Field format incorrect",
                    properties, formData);
        }
    }
    
    /**
     * Validates that the value of {@code id} is non-empty.
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateRequired(String id, Dataset properties,
            Dataset formData) {
        String inputValue = formData.get(id);
        if (inputValue == null || inputValue.length() == 0) {
            return errorMessage("Required value", properties, formData);
        }
        return null;
    }
}
