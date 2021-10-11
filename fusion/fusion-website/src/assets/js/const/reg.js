/*!
 * @author claude
 */

export const EMAILREG = /^(?:\w+\.?)*\w+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;

export const PASSWORDREG = /^(?![\d]+$)(?![a-zA-Z]+$)(?![^\da-zA-Z]+$).{8,20}$/;
