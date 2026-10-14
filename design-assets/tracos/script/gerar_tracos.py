import sys, math, random
from PIL import Image, ImageDraw
out = sys.argv[1]
SS = 4  # supersample over the 2x asset
def hexrgb(h): return tuple(int(h[i:i+2],16) for i in (1,3,5))
def stroke(d, p0, p1, w, rnd, amp):
    (x0,y0),(x1,y1) = p0,p1
    L = math.hypot(x1-x0,y1-y0); n = max(int(L/3),2)
    nx, ny = -(y1-y0)/L, (x1-x0)/L
    ph = [rnd.uniform(0,6.28) for _ in range(3)]
    fr = [rnd.uniform(0.6,1.4)/L*6.28*k for k in (1,2.3,5.1)]
    for i in range(n):
        t0, t1 = i/n, (i+1)/n
        def pt(t):
            s = t*L
            off = amp*(0.6*math.sin(fr[0]*s+ph[0])+0.3*math.sin(fr[1]*s+ph[1])+0.15*math.sin(fr[2]*s+ph[2]))
            return (x0+(x1-x0)*t+nx*off, y0+(y1-y0)*t+ny*off)
        ww = w*(0.8+0.35*math.sin(fr[1]*t0*L+ph[2])+rnd.uniform(-0.05,0.05))
        a,b = pt(t0),pt(t1)
        r = max(ww/2,0.6)
        d.line([a,b], fill=255, width=max(int(round(ww)),1))
        d.ellipse([a[0]-r,a[1]-r,a[0]+r,a[1]+r], fill=255)
def rect(d, x0,y0,x1,y1,w,rnd,amp,over):
    stroke(d,(x0-over*rnd.random(),y0),(x1+over*rnd.random(),y0),w,rnd,amp)
    stroke(d,(x1,y0-over*rnd.random()),(x1,y1+over*rnd.random()),w,rnd,amp)
    stroke(d,(x1+over*rnd.random(),y1),(x0-over*rnd.random(),y1),w,rnd,amp)
    stroke(d,(x0,y1+over*rnd.random()),(x0,y0-over*rnd.random()),w,rnd,amp)
def save(mask, color, name, size):
    m = mask.resize(size, Image.LANCZOS)
    img = Image.new("RGBA", size, hexrgb(color)+(0,))
    img.putalpha(m)
    img.save(f"{out}/{name}.png")
def make(name, W, H, draw_fn, colors, seed=1):
    mask = Image.new("L",(W*SS,H*SS),0); d = ImageDraw.Draw(mask)
    draw_fn(d, random.Random(seed))
    for suffix,c in colors.items(): save(mask,c,f"{name}-{suffix}",(W,H))
S=SS
pal = {"dark":"#3A2E20","light":"#C9B27C"}
# double frame 192x192 (2x), slice 32
make("frame2",192,192,lambda d,r:(rect(d,6*S,6*S,186*S,186*S,4.2*S,r,1.6*S,5*S),rect(d,16*S,16*S,176*S,176*S,2.2*S,r,1.2*S,2*S)),pal,3)
# single frame 96x96 (2x), slice 16
make("frame1",96,96,lambda d,r:rect(d,5*S,5*S,91*S,91*S,2.6*S,r,1.0*S,3*S),
     {"dark":"#3A2E20","light":"#C9B27C","olive":"#5E6252","olivel":"#9CA08C","seal":"#8C3A2B","seall":"#C4705E"},5)
# checkbox 56x56 (2x)
make("box",56,56,lambda d,r:rect(d,6*S,6*S,50*S,50*S,3.2*S,r,1.0*S,3*S),pal,7)
# X mark 56x56
def xm(d,r):
    stroke(d,(12*S,11*S),(45*S,46*S),4.5*S,r,1.2*S); stroke(d,(45*S,12*S),(13*S,44*S),4.0*S,r,1.2*S)
make("xmark",56,56,xm,{"seal":"#8C3A2B","seall":"#C4705E"},9)
# horizontal rule 1200x8
make("rule",1200,8,lambda d,r:stroke(d,(2*S,4*S),(1198*S,4*S),2.6*S,r,1.2*S),pal,11)
# notebook line 1200x56 (line near bottom, 28px@1x rows)
make("ledger",1200,56,lambda d,r:stroke(d,(0,53*S),(1200*S,53*S),1.4*S,r,1.0*S),{"dark":"#C9BC95","light":"#3A372C"},13)
# vertical rule 8x1600
make("vrule",8,1600,lambda d,r:stroke(d,(4*S,2*S),(4*S,1598*S),2.2*S,r,1.2*S),pal,15)
# plus 40x40
def plus(d,r):
    stroke(d,(20*S,7*S),(20*S,33*S),3.6*S,r,0.8*S); stroke(d,(7*S,20*S),(33*S,20*S),3.6*S,r,0.8*S)
make("plus",40,40,plus,pal,17)
