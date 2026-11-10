package chbcars.util;

/**
 * Defensive-programming helpers. Every method checks one thing and throws a
 * ValidationException with a friendly message, so forms can reuse them.
 */
public final class Validator {

    private Validator() {
    }

    public static String requireText(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
        return value.trim();
    }

    public static String requireText(String value, String field, int min, int max) throws ValidationException {
        String text = requireText(value, field);
        if (text.length() < min || text.length() > max) {
            throw new ValidationException(field + " must be " + min + " to " + max + " characters long.");
        }
        return text;
    }

    public static String optionalText(String value, String field, int max) throws ValidationException {
        String text = value == null ? "" : value.trim();
        if (text.length() > max) {
            throw new ValidationException(field + " must not exceed " + max + " characters.");
        }
        return text;
    }

    public static String requireName(String value, String field) throws ValidationException {
        String text = requireText(value, field, 2, 60);
        if (!text.matches("[\\p{L} .'-]+")) {
            throw new ValidationException(field + " may only contain letters, spaces, dots, hyphens and apostrophes.");
        }
        return text;
    }

    public static double parseMoney(String value, String field) throws ValidationException {
        String text = requireText(value, field).replace(",", "").replace("\u20B1", "");
        try {
            double number = Double.parseDouble(text);
            if (number <= 0 || number > 1_000_000_000) {
                throw new ValidationException(field + " must be greater than 0.");
            }
            return Math.round(number * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a valid number.");
        }
    }

    public static int parseInt(String value, String field, int min, int max) throws ValidationException {
        String text = requireText(value, field).replace(",", "");
        try {
            int number = Integer.parseInt(text);
            if (number < min || number > max) {
                throw new ValidationException(field + " must be between " + min + " and " + max + ".");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }

    public static String requirePhone(String value) throws ValidationException {
        String text = requireText(value, "Contact number");
        if (!text.matches("(\\+?63|0)9\\d{9}") && !text.matches("\\d{7,11}")) {
            throw new ValidationException("Contact number must be a valid phone number (e.g. 09171234567).");
        }
        return text;
    }

    public static String optionalEmail(String value) throws ValidationException {
        String text = value == null ? "" : value.trim();
        if (!text.isEmpty() && !text.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) {
            throw new ValidationException("Email address is not valid.");
        }
        return text;
    }

    public static String requireUsername(String value) throws ValidationException {
        String text = requireText(value, "Username");
        if (!text.matches("[A-Za-z0-9._]{4,20}")) {
            throw new ValidationException("Username must be 4-20 characters (letters, numbers, dot or underscore).");
        }
        return text;
    }

    public static void checkPasswordStrength(String password) throws ValidationException {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new ValidationException("Password must contain both letters and numbers.");
        }
        if (password.contains(" ")) {
            throw new ValidationException("Password must not contain spaces.");
        }
    }

    /** Returns a label for the live strength indicator on the sign-up form. */
    public static String passwordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;
        return score <= 1 ? "Weak" : score == 2 ? "Fair" : score == 3 ? "Good" : "Strong";
    }

    public static void requireSelected(Object value, String field) throws ValidationException {
        if (value == null) {
            throw new ValidationException("Please select " + field + ".");
        }
    }
}
