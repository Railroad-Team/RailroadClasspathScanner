package dev.turtywurty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompilerResponse {
    private List<ResponseMethod> methods;

    public record ResponseMethod(String name, String returnType, Map<String, String> parameters) {}
}
