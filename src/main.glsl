

#define outer vec3(.1 , 1.0, 1.)
#define inner vec3(.15,0.5, 1.)
#define pi 3.14159

uniform vec2 size;
uniform vec2 topleft;
uniform vec2 resolution;
uniform float time;


uniform float blockPositionsX    [100];
uniform float blockPositionsY    [100];
uniform float blockSizesX        [100];
uniform float blockSizesY        [100];
uniform float blockDeathframes   [100];
uniform float blockHealths       [100];
uniform float blockMaxHealths    [100];
uniform float blockLastHits      [100];

uniform float ballPositionsX     [10];
uniform float ballPositionsY     [10];
uniform float ballRs             [10];

uniform float platformPositionsX [10];
uniform float platformPositionsY [10];
uniform float platformSizesX     [10];
uniform float platformSizesY     [10];
uniform float platformLastHits   [10];

uniform float smoothstepOffset;


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

float maxrect(vec2 uv, vec2 c){
	return max(abs(c.x-uv.x), abs(c.y-uv.y));
}

void main(){
    vec2 bguv = (gl_FragCoord.xy-topleft.xy) / size.xy;
    vec2 c = vec2(.5);
    float d = maxrect(bguv, c)*10+distance(bguv, c)*20.;
    float t = time/80.;
    vec2 uv = gl_FragCoord.xy / size.xy;
    vec4 hsb = vec4(d*.02-t/20., 1., 0.1, 1.);
    for(int i = 0; i < 100; i++){
        if(blockDeathframes[i] > 0. || blockPositionsX[i] == 0) continue;
        vec2 blockPos = vec2(blockPositionsX[i] / size.x, 1.-blockPositionsY[i] / size.y);
        vec2 blockSize = vec2(blockSizesX[i] / size.x/2., blockSizesY[i] / size.y/2.);
        vec3 rect = rect(uv, blockPos, blockSize, vec2(smoothstepOffset));
        float healthIndicator = map(blockMaxHealths[i]-blockHealths[i], 0., blockMaxHealths[i], 0.,.8);
        float hitAnimationFrame = (time-blockLastHits[i])/30.;
        hsb.r += rect.r * (.1-healthIndicator/2.);
        hsb.b += rect.r * (.5+(clamp(hitAnimationFrame, 0., 1.5)));
    }
    for(int i = 0; i < 10; i++){
        vec2 ballPos = vec2(ballPositionsX[i]/size.x, 1.-ballPositionsY[i]/size.y);
        vec3 ellipse = ellipse(uv, ballPos, ballRs[i]/size.x/2.);
        hsb.g -= clamp( ellipse.r, 0., hsb.g);
        hsb.b += clamp( ellipse.r*2., 0., 1.-hsb.b);
    }
    for(int i = 0; i < 10; i++){
        if(platformPositionsX[i] == 0.) continue;
        vec2 platformPos = vec2(platformPositionsX[i]/min(size.x, size.y),
                            1.-platformPositionsY[i]/min(size.x, size.y));
        vec2 platformSize = vec2(platformSizesX[i], platformSizesY[i])/size;
        vec3 platform = rect(uv, platformPos, platformSize/2., vec2(smoothstepOffset));
        float hitAnimationFrame = (time-platformLastHits[i])/30.;
        hsb.b += platform.r * (.5+(clamp(hitAnimationFrame, 0., 1.5)));
    }
    gl_FragColor = vec4(hsb2rgb(hsb.xyz), hsb.a);
}