package SPEDFiscal;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ValidationEventField extends ValidationEvent {
    private final Field field;
    private final Register register;

    ValidationEventField(Register register, Field field, String message) {
        super(register.getName() + "." + field.getName() + ": \"" + message + "\"" );

        this.register = register;
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public Register getRegister() {
        return register;
    }
}

class validatorHelper {
    public static boolean validateCPF(String cpf) {
        if (cpf.equals("00000000000") ||
                cpf.equals("11111111111") ||
                cpf.equals("22222222222") || cpf.equals("33333333333") ||
                cpf.equals("44444444444") || cpf.equals("55555555555") ||
                cpf.equals("66666666666") || cpf.equals("77777777777") ||
                cpf.equals("88888888888") || cpf.equals("99999999999") ||
                (cpf.length() != 11))
            return(false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        sm = 0;
        peso = 10;
        for (i=0; i<9; i++) {
            num = (int)(cpf.charAt(i) - 48);
            sm = sm + (num * peso);
            peso = peso - 1;
        }

        r = 11 - (sm % 11);
        if ((r == 10) || (r == 11))
            dig10 = '0';
        else
            dig10 = (char)(r + 48);

        sm = 0;
        peso = 11;
        for(i=0; i<10; i++) {
            num = (int)(cpf.charAt(i) - 48);
            sm = sm + (num * peso);
            peso = peso - 1;
        }

        r = 11 - (sm % 11);
        if ((r == 10) || (r == 11))
            dig11 = '0';
        else
            dig11 = (char)(r + 48);

        return (dig10 == cpf.charAt(9)) && (dig11 == cpf.charAt(10));
    }
}

public class RegisterValidator {
    void validate(Register register, ValidationListener validationListener) {
        for (Map.Entry<String, Field<?>> e : register.getFields().entrySet()) {
            Field<?> field = e.getValue();

            String fieldFormatName = register.getName() + "." + field.getName();
            String formattedValue = register.getDefinitions().formatField(field, fieldFormatName);
            String required = register.getDefinitions().getRequired(register.getName(), field.getName());

            if (!required.equals("O") && formattedValue.isEmpty()) {
                continue;
            }

            if (required.equals("O") && formattedValue.isEmpty()) {
                String message = "Campo ogrigatório não informado";
                validationListener.onErrorMessage(new ValidationEventField(register, field, message));
                continue;
            }

            Validation validation = register.getDefinitions().getValidation(register.getName(), field.getName());
            String failMessage = validation != null ? validation.getFailMessage() : "";

            if (validation instanceof ValidationRegex validationRegex) {
                String expression = validationRegex.getExpression();

                if (!expression.isEmpty()) {
                    Pattern pattern = Pattern.compile(expression);
                    Matcher matcher = pattern.matcher(formattedValue);
                    if (!matcher.matches()) {
                        String message = register.getName() + "." + field.getName() + ": \"" + formattedValue + "\": \"" + failMessage + "\"";
                        validationListener.onErrorMessage(new ValidationEvent(message));
                    }
                }
            }

            if (validation instanceof ValidationScript validationScript) {
                String script = validationScript.getScript();

                ScriptEngineManager mgr = new ScriptEngineManager();
                var engine = mgr.getEngineByName("JS");

                try {
                    engine.put("param", formattedValue);
                    engine.eval(script);
                    engine.eval("var objMessage = {message: ''}");
                    engine.eval("var isValid = %s(param, objMessage);".formatted(validationScript.getName()));
                    engine.eval("var message = objMessage.message");
                    var isValidObject = engine.get("isValid");
                    var message = engine.get("message");

                    if (isValidObject instanceof Boolean isValid) {
                        if (!isValid) {
                            message =  register.getName() + "." + field.getName() + ": \"" + formattedValue + "\": \"" + message + "\"";
                            validationListener.onErrorMessage(new ValidationEvent((String) message));
                        }
                    }
                } catch (ScriptException se) {
                    throw new RuntimeException(se);
                }
            }

            String innerValidation = register.getDefinitions().getInnerValidation(register.getName(), field.getName());

            if (innerValidation != null && innerValidation.equals("cpf")) {
                if (!validatorHelper.validateCPF(formattedValue)) {
                    String message = register.getName() + "." + field.getName() + ": \"" + formattedValue + "\": \"" + "Digito verificador inválido" + "\"";
                    validationListener.onErrorMessage(new ValidationEvent(message));
                }
            }
        }
    }
}