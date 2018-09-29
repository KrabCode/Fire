
#define outer vec3(.1 , 1.0, 1.)
#define inner vec3(.15,0.5, 1.)

uniform vec2 size;
uniform vec2 topleft;
uniform vec2 resolution;
uniform float time;

uniform float[98] blockPositionsX;
uniform float[98] blockPositionsY;
uniform float[98] blockSizesX;
uniform float[98] blockSizesY;
uniform float[98] blockDeathframes;

uniform float[30] ballPositionsX;
uniform float[30] ballPositionsY;
uniform float[30] ballRs;

uniform float smoothstepOffset = 0.01;


vec3 hsb2rgb( in vec3 c){
 vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0), 6.0)-3.0)-1.0, 0.0, 1.0 );
 rgb = rgb*rgb*(3.0-2.0*rgb);
 return c.z * mix(vec3(1.0), rgb, c.y);
}

vec3 ellipse(vec2 uv, vec2 c, float r){
  float d = distance(uv,c);
  return vec3(1.-smoothstep(r-smoothstepOffset, r, d));
}

float map(float x, float a1, float a2, float b1, float b2){
  return b1 + (b2-b1) * (x-a1) / (a2-a1);
}


vec3 rect(vec2 uv, vec2 c, vec2 s, vec2 off){
   float p = max(smoothstep(c.x+s.x,c.x+s.x+off.x, uv.x), smoothstep(c.y+s.y,c.y+s.y+off.y,uv.y));
   float q = max(smoothstep(c.x-s.x,c.x-s.x-off.x, uv.x), smoothstep(c.y-s.y,c.y-s.y-off.y,uv.y));
   return vec3(1.-max(p,q));
}

void main(){
    vec2 uv = (gl_FragCoord.xy-topleft.xy) / size.xy;
    vec2 c = vec2(.5);
    float d = distance(uv, c)*2.;
    vec4 hsb = vec4(1.-uv.x*.2+time/20., 1., 0.2, 1.);
    for(int i = 0; i < 98; i++){
        if(blockDeathframes[i] > 0.) continue;
        vec2 blockPos = vec2(blockPositionsX[i] / min(size.x, size.y), 1.-blockPositionsY[i] / size.y);
        vec2 blockSize = vec2(blockSizesX[i] / size.x/2., blockSizesY[i] / size.y/2.);
        vec3 rect = rect(uv, blockPos, blockSize, vec2(smoothstepOffset));
        hsb.b += rect.r;
    }
    for(int i = 0; i < 1; i++){

        vec2 ballPos = vec2(ballPositionsX[i]/min(size.x, size.y)-ballRs[i]/2.,
                            ballPositionsY[i]/min(size.x, size.y)+ballRs[i]/2.);
        vec3 ellipse = ellipse(uv, ballPos, ballRs[i]);

        hsb.z *= 1.+ellipse.r*10.;
    }
    gl_FragColor = vec4(hsb2rgb(hsb.xyz), hsb.a);
}