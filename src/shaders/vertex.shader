#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoords;
layout (location = 2) in vec3 aNormal;

out vec3 FragPos;
out vec2 TextureCoord;
out vec3 Normal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform sampler2D texture2;

void main()
{
   TextureCoord = aTexCoords;

   float offset = texture(texture2, TextureCoord).x - 0.5;
   vec3 shiftedPosition = vec3(aPos.x, aPos.y + offset, aPos.z);
   FragPos = vec3(model * vec4(shiftedPosition, 1.0));
   Normal = mat3(transpose(inverse(model))) * aNormal;

   gl_Position = projection * view * model * vec4(shiftedPosition, 1.0);
}