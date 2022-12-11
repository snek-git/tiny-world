#version 330 core

out vec4 FragColor;

in vec3 Normal;
in vec2 TextureCoord;
in vec3 FragPos;

uniform vec3 lightDir;
uniform vec3 viewPos;
uniform vec3 lightColor;

uniform sampler2D texture1;

void main()
{

     //diffuse
    vec3 norm = normalize(Normal);
    float diff = max(dot(norm, lightDir), 0.0) * 0.6f;
    vec3 diffuse = diff * lightColor;

    FragColor = vec4(diffuse, 1.0) * texture(texture1, TextureCoord);
}