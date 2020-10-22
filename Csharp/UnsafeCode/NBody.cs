using System;
using System.Numerics;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;

namespace Benchmarks.UnsafeCode
{
    public static class NBody
    {
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        static unsafe byte GetByte(double* pCrb, double Ciby)
        {
            var res = 0;
            for (var i = 0; i < 8; i += 2)
            {
                var vCrbx = Unsafe.Read<Vector<double>>(pCrb + i);
                var vCiby = new Vector<double>(Ciby);
                var Zr = vCrbx;
                var Zi = vCiby;
                int b = 0, j = 49;
                do
                {
                    for (int counter = 0; counter < 7; counter++)
                    {
                        var nZr = Zr * Zr - Zi * Zi + vCrbx;
                        var ZrZi = Zr * Zi;
                        Zi = ZrZi + ZrZi + vCiby;
                        Zr = nZr;
                        j--;
                    }

                    var t = Zr * Zr + Zi * Zi;
                    if (t[0] > 4.0)
                    {
                        b |= 2;
                        if (b == 3) break;
                    }

                    if (t[1] > 4.0)
                    {
                        b |= 1;
                        if (b == 3) break;
                    }
                } while (j > 0);

                res = (res << 2) + b;
            }

            return (byte) (res ^ -1);
        }

        public static unsafe void NbodyRun(int size = 200)
        {
            //Console.Out.WriteAsync(String.Concat("P4\n", size, " ", size, "\n"));
            var Crb = new double[size + 2];
            var lineLength = size >> 3;
            var data = new byte[size * lineLength];
            fixed (double* pCrb = &Crb[0])
            fixed (byte* pdata = &data[0])
            {
                var value = new Vector<double>(
                    new double[] {0, 1, 0, 0, 0, 0, 0, 0}
                );
                var invN = new Vector<double>(2.0 / size);
                var onePtFive = new Vector<double>(1.5);
                var step = new Vector<double>(2);
                for (var i = 0; i < size; i += 2)
                {
                    Unsafe.Write(pCrb + i, value * invN - onePtFive);
                    value += step;
                }

                var _Crb = pCrb;
                var _pdata = pdata;
                Parallel.For(0, size, y =>
                {
                    var Ciby = _Crb[y] + 0.5;
                    for (var x = 0; x < lineLength; x++)
                    {
                        _pdata[y * lineLength + x] = GetByte(_Crb + x * 8, Ciby);
                    }
                });
                // Console.OpenStandardOutput().Write(data, 0, data.Length);
            }
        }
    }
}