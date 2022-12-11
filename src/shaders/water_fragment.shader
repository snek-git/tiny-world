#version 330 core

out vec4 FragColor;

in vec3 Normal;
in vec2 TextureCoord;
in vec3 FragPos;

uniform vec3 lightDir;
uniform vec3 viewPos;
uniform vec3 lightColor;

uniform sampler2D texture1;

uniform float waterDisplacement;

void main()
{
     //ambient
    float ambientStrength = 0.1;
    vec3 ambient = ambientStrength * lightColor;

     //diffuse
    vec3 norm = normalize(Normal);
    float diff = max(dot(norm, lightDir), 0.0) * 0.6f;
    vec3 diffuse = diff * lightColor ;

     //specular
    float specularStrength = 0.1;
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 4);
    vec3 specular = specularStrength * spec * lightColor;

    vec3 result = (ambient + diffuse + specular);

    vec2 FlowingCoord = vec2(TextureCoord.x + waterDisplacement * 0.0001, TextureCoord.y);

    FragColor = vec4(result, 1.0) * vec4(texture(texture1, FlowingCoord).xyz, 0.7);
}