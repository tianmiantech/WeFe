/*!
 * @author claude
 * regExp
 */

// email
export const EMAILREG = /^(?:\w+\.?)*\w+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;

/**
 * Password strength
 * include special symbol combination of numbers and letters
 * 8-30 long
 */
export const PASSWORDREG = /^(?![\d]+$)(?![a-zA-Z]+$)(?![^\da-zA-Z]+$).{8,30}$/;
