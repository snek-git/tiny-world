#version 330 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 4) out;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform sampler2D grassDistr;
uniform float size;

in vec2 TextureCoord[];

out vec2 outTextureCoord;
out vec3 Normal;

vec3 calcNormal(vec4 pos0, vec4 pos1, vec4 pos2){
    vec3 a = vec3(pos1) - vec3(pos0);
    vec3 b = vec3(pos2) - vec3(pos0);
    return normalize(cross(a, b));
}



void generateObject(vec4 position, mat4 modelViewPos){

    Normal = calcNormal(gl_in[0].gl_Position, gl_in[1].gl_Position, gl_in[2].gl_Position);


    gl_Position = modelViewPos * (position + vec4(-size / 2, 0.0, 0.0, 0.0));
    outTextureCoord = vec2(0, 0);
    EmitVertex();

    gl_Position = modelViewPos * (position + vec4(-size / 2, size, 0.0, 0.0));
    outTextureCoord = vec2(0, 1);
    EmitVertex();

    gl_Position = modelViewPos * (position + vec4(size / 2, 0.0, 0.0, 0.0));
    outTextureCoord = vec2(1, 0);
    EmitVertex();

    gl_Position = modelViewPos * (position + vec4(size / 2, size, 0.0, 0.0));
    outTextureCoord = vec2(1, 1);
    EmitVertex();

}





void main() {
        if (texture(grassDistr, (TextureCoord[0] + TextureCoord[1] + TextureCoord[2]) / 3).x >= 0.1)
            generateObject((gl_in[0].gl_Position + gl_in[1].gl_Position + gl_in[2].gl_Position) / 3, projection * view * model);

    EndPrimitive();
}