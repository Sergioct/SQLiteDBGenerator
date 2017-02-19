#import <Foundation/Foundation.h>

#import "sqlite3.h"

@interface Producto : NSObject{
	NSInteger myid;
	NSString * nombre;
	double coste;
}

	@property (nonatomic) NSInteger myid;
	@property (nonatomic) NSString * nombre;
	@property (nonatomic) double coste;
@end