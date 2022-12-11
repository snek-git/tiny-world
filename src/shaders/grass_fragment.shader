#version 330 core

out vec4 FragColor;

in vec3 Normal;
in vec2 outTextureCoord;
in vec3 FragPos;

uniform vec3 lightDir;
uniform vec3 viewPos;
uniform vec3 lightColor;

uniform sampler2D grassTexture;

void main()
{

    //diffuse
    vec3 norm = normalize(Normal);
    float diff = 1;
    vec3 diffuse = diff * lightColor;

    vec4 grassColor = texture(grassTexture, outTextureCoord);

    FragColor = vec4(diffuse * vec3(grassColor), grassColor.w);

    if(FragColor.a < 0.1)
        discard;
}