#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "Segunda.h"

@interface SegundaDAO : NSObject{
	sqlite3 *db;
}

+ (SegundaDAO *) instance;

- (void) createObject:(Segunda*) item;

- (NSMutableArray *) getAll;

- (Segunda *) getById:(NSInteger)auxid;

- (void) updateObject:(Segunda*) item;

- (void) deleteObject:(NSInteger)auxid;

@end